import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

   private UTXOPool utxoPool;

   public TxHandler(UTXOPool up) {
      utxoPool = new UTXOPool(up);
   }

   // assuming all utxo's required by this transaction will be in utxo pool
   public boolean isValidTx(Transaction tx) {
      double totalInput = 0;
      HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
      for (int i = 0; i < tx.getInputs().size(); i++) {
         Transaction.Input in = tx.getInput(i);
         UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
         if (!utxosSeen.add(ut))
            return false;
         Transaction.Output op = utxoPool.getTxOutput(ut);
         if (op == null)
            return false;
         RSAKey address = op.address;
         if (!address.verifySignature(tx.getRawDataToSign(i), in.signature))
            return false;
         totalInput += op.value;
      }
      double totalOutput = 0;
      ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
      for (Transaction.Output op : txOutputs) {
         if (op.value < 0)
            return false;
         totalOutput += op.value;
      }
      return (totalInput >= totalOutput);
   }

   private boolean inPool(Transaction tx) {
      ArrayList<Transaction.Input> inputs = tx.getInputs();
      Transaction.Input in;
      UTXO ut;
      for (int i = 0; i < inputs.size(); i++) {
         in = inputs.get(i);
         ut = new UTXO(in.prevTxHash, in.outputIndex);
         if (!utxoPool.contains(ut))
            return false;
      }
      return true;
   }

   private void updatePool(Transaction tx) {
      for (int i = 0; i < tx.getInputs().size(); i++) {
         Transaction.Input in = tx.getInput(i);
         utxoPool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));
      }
      for (int i = 0; i < tx.getOutputs().size(); i++) {
         Transaction.Output out = tx.getOutput(i);
         utxoPool.addUTXO(new UTXO(tx.getHash(), i), out);
      }
   }

   // do not change actual utxo pool because maintained a separate copy
   public Transaction[] handleTxs(Transaction[] allTx) {
      Transaction[] stuckTxs = new Transaction[allTx.length];
      for (int i = 0; i < allTx.length; i++)
         stuckTxs[i] = allTx[i];
      Transaction[] tempTxs = new Transaction[allTx.length];
      Transaction[] successTxs = new Transaction[allTx.length];
      int tempCounter = 0, successCounter = 0;
      int stuckSize = allTx.length;
      while (true) {
         boolean change = false;
         tempCounter = 0;
         for (int i = 0; i < stuckSize; i++) {
            if (inPool(stuckTxs[i])) {
               if (isValidTx(stuckTxs[i])) {
                  change = true;
                  updatePool(stuckTxs[i]);
                  successTxs[successCounter++] = stuckTxs[i];
               }
            } else {
               tempTxs[tempCounter++] = stuckTxs[i];
            }
         }
         if (change) {
            for (int i = 0; i < tempCounter; i++) {
               stuckTxs[i] = tempTxs[i];
            }
            stuckSize = tempCounter;
         } else {
            break;
         }
      }
      Transaction[] result = new Transaction[successCounter];
      for (int i = 0; i < successCounter; i++)
         result[i] = successTxs[i];
      return result;
   }
}

