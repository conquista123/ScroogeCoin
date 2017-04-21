import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Transaction {

   public class Input {
      public byte[] prevTxHash;   // hash of the Transaction whose output is being used
      public int outputIndex;     // used output's index in the previous transaction 
      public byte[] signature;    // the signature produced to check validity

      public Input(byte[] prevHash, int index) {
         if (prevHash == null)
            prevTxHash = null;
         else
            prevTxHash = Arrays.copyOf(prevHash, prevHash.length);
         outputIndex = index;
      }
      public void addSignature(byte[] sig) {
         if (sig == null)
            signature = null;
         else
            signature = Arrays.copyOf(sig, sig.length);
      }
   }

   public class Output {
      public double value;        // value in bitcoins of the output
      public RSAKey address;      // the address or public key of the recipient

      public Output(double v, RSAKey addr) {
         value = v;
         address = addr;
      }
   }

   private byte[] hash;    // hash of the transaction, its unique id
   private ArrayList<Input> inputs;   // inputs
   private ArrayList<Output> outputs; // outputs

   public Transaction() {
      inputs = new ArrayList<Input>();
      outputs = new ArrayList<Output>();
   }

   public Transaction(Transaction tx) {
      hash = tx.hash.clone();
      inputs = new ArrayList<Input>(tx.inputs);
      outputs = new ArrayList<Output>(tx.outputs);
   }

   public void addInput(byte[] prevTxHash, int outputIndex) {
      Input in = new Input(prevTxHash, outputIndex);
      inputs.add(in);
   }

   public void addOutput(double value, RSAKey address) {
      Output op = new Output(value, address);
      outputs.add(op);
   }

   public void removeInput(int index) {
      inputs.remove(index);
   }

   public void removeInput(UTXO ut) {
      for (int i = 0; i < inputs.size(); i++) {
         Input in = inputs.get(i);
         UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
         if (u.equals(ut)) {
            inputs.remove(i);
            return;
         }
      }
   }

   public byte[] getRawDataToSign(int index) {
      // ith input and all outputs
      ArrayList<Byte> sigData = new ArrayList<Byte>();
      if (index > inputs.size()) 
         return null;
      Input in = inputs.get(index);
      byte[] prevTxHash = in.prevTxHash;
      ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8);
      b.putInt(in.outputIndex);
      byte[] outputIndex = b.array();
      if (prevTxHash != null)
         for (int i = 0; i < prevTxHash.length; i++)
            sigData.add(prevTxHash[i]);
      for (int i = 0; i < outputIndex.length; i++)
         sigData.add(outputIndex[i]);
      for (Output op : outputs) {
         ByteBuffer bo = ByteBuffer.allocate(Double.SIZE/8);
         bo.putDouble(op.value);
         byte[] value = bo.array();
         byte[] addressExponent = op.address.getExponent().toByteArray();
         byte[] addressModulus = op.address.getModulus().toByteArray();
         for (int i = 0; i < value.length; i++)
            sigData.add(value[i]);
         for (int i = 0; i < addressExponent.length; i++)
            sigData.add(addressExponent[i]);
         for (int i = 0; i < addressModulus.length; i++)
            sigData.add(addressModulus[i]);
      }
      byte[] sigD = new byte[sigData.size()];
      int i = 0;
      for(Byte sb : sigData)
         sigD[i++] = sb;
      return sigD;
   }

   public void addSignature(byte[] signature, int index) {
      inputs.get(index).addSignature(signature);
   }

   public byte[] getRawTx() {
      ArrayList<Byte> rawTx = new ArrayList<Byte>();
      for (Input in : inputs) {
         byte[] prevTxHash = in.prevTxHash;
         ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8);
         b.putInt(in.outputIndex);
         byte[] outputIndex = b.array();
         byte[] signature = in.signature;
         if (prevTxHash != null)
            for (int i = 0; i < prevTxHash.length; i++)
               rawTx.add(prevTxHash[i]);
         for (int i = 0; i < outputIndex.length; i++)
            rawTx.add(outputIndex[i]);
         if (signature != null)
            for (int i = 0; i < signature.length; i++)
               rawTx.add(signature[i]);
      }
      for (Output op : outputs) {
         ByteBuffer b = ByteBuffer.allocate(Double.SIZE/8);
         b.putDouble(op.value);
         byte[] value = b.array();
         byte[] addressExponent = op.address.getExponent().toByteArray();
         byte[] addressModulus = op.address.getModulus().toByteArray();
         for (int i = 0; i < value.length; i++)
            rawTx.add(value[i]);
         for (int i = 0; i < addressExponent.length; i++)
            rawTx.add(addressExponent[i]);
         for (int i = 0; i < addressModulus.length; i++)
            rawTx.add(addressModulus[i]);
      }
      byte[] tx = new byte[rawTx.size()];
      int i = 0;
      for(Byte b : rawTx)
         tx[i++] = b;
      return tx;
   }

   public void finalize() {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(getRawTx());
         hash = md.digest();
      } catch(NoSuchAlgorithmException x) {
         x.printStackTrace(System.err);
      }
   }

   public void setHash(byte[] h) {
      hash = h;
   }
   
   public byte[] getHash() {
      return hash;
   }

   public ArrayList<Input> getInputs() {
      return inputs;
   }

   public ArrayList<Output> getOutputs() {
      return outputs;
   }

   public Input getInput(int index) {
      if (index < inputs.size()) {
         return inputs.get(index);
      }
      return null;
   }

   public Output getOutput(int index) {
      if (index < outputs.size()) {
         return outputs.get(index);
      }
      return null;
   }

   public int numInputs() {
      return inputs.size();
   }

   public int numOutputs() {
      return outputs.size();
   }
}
