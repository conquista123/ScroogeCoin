import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;

class TestIsValidTx {
   
   public int nPeople;
   public int nUTXOTx;
   public int maxUTXOTxOutput;
   public double maxValue;
   public int nTxPerTest;
   public int maxInput;
   public int maxOutput;
   public double pCorrupt;

   public PRGen prGen;
   public ArrayList<RSAKeyPair> people;
   public HashMap<UTXO, RSAKeyPair> utxoToKeyPair;
   public UTXOPool utxoPool;
   public ArrayList<UTXO> utxoSet;
   public int maxValidInput;

   public TxHandler txHandler;

   public TestIsValidTx(int nPeople, int nUTXOTx, 
                           int maxUTXOTxOutput, double maxValue, int nTxPerTest, int maxInput, int maxOutput, double pCorrupt) throws FileNotFoundException, IOException {
      
      this.nPeople = nPeople;
      this.nUTXOTx = nUTXOTx;
      this.maxUTXOTxOutput = maxUTXOTxOutput;
      this.maxValue = maxValue;
      this.nTxPerTest = nTxPerTest;
      this.maxInput = maxInput;
      this.maxOutput = maxOutput;
      this.pCorrupt = pCorrupt;
      
      byte[] key = new byte[32];
      for (int i = 0; i < 32; i++) {
         key[i] = (byte) 1;
      }
      
      prGen = new PRGen(key);
      
      people = new ArrayList<RSAKeyPair>();
      for (int i = 0; i < nPeople; i++)
         people.add(new RSAKeyPair(prGen, 265));
      
      HashMap<Integer, RSAKeyPair> keyPairAtIndex = new HashMap<Integer, RSAKeyPair>();
      utxoToKeyPair = new HashMap<UTXO, RSAKeyPair>();

      utxoPool = new UTXOPool();

      for (int i = 0; i < nUTXOTx; i++) {
         int num = SampleRandom.randomInt(maxUTXOTxOutput) + 1;
         Transaction tx = new Transaction();
         for (int j = 0; j < num; j++) {
            // pick a random public address
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = SampleRandom.randomDouble(maxValue);
            tx.addOutput(value, addr);
            keyPairAtIndex.put(j, people.get(rIndex));
         }
         tx.finalize();
         // add all tx outputs to utxo pool
         for (int j = 0; j < num; j++) {
            UTXO ut = new UTXO(tx.getHash(), j);
            utxoPool.addUTXO(ut, tx.getOutput(j));
            utxoToKeyPair.put(ut, keyPairAtIndex.get(j));
         }
      }

      utxoSet = utxoPool.getAllUTXO();
      maxValidInput = Math.min(maxInput, utxoSet.size());

      txHandler = new TxHandler(new UTXOPool(utxoPool));
   }

   public int test1() {
      System.out.println("Test 1: test isValidTx() with valid transactions");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput) + 1;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (!txHandler.isValidTx(tx)) {
            passes = false;
         }
      }
      return UtilCOS.printPassFail(passes);
   }

   public int test2() {
      System.out.println("Test 2: test isValidTx() with transactions containing signatures of incorrect data");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput) + 1;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            byte[] rawData = tx.getRawDataToSign(j);
            if (Math.random() < pCorrupt) {
               rawData[0]++;
               uncorrupted = false;
            }
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(rawData), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }

   public int test3() {
      System.out.println("Test 3: test isValidTx() with transactions containing signatures using incorrect private keys");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput-1) + 2;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            RSAKeyPair keyPair = utxoToKeyPair.get(utxoAtIndex.get(j));
            if (Math.random() < pCorrupt) {
               int index = people.indexOf(keyPair);
               keyPair = people.get((index + 1) % nPeople);
               uncorrupted = false;
            }
            tx.addSignature(keyPair.getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }
   
   public int test4() {
      System.out.println("Test 4: test isValidTx() with transactions whose total output value exceeds total input value");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput) + 1;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue) {
               if (Math.random() < pCorrupt) {
                  uncorrupted = false;
               } else {
                  break;
               }
            }
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }

   public int test5() {
      System.out.println("Test 5: test isValidTx() with transactions that claim outputs not in the current utxoPool");

      boolean passes = true;

      ArrayList<RSAKeyPair> peopleExtra = new ArrayList<RSAKeyPair>();
      for (int i = 0; i < nPeople; i++)
         peopleExtra.add(new RSAKeyPair(prGen, 265));
      
      HashMap<Integer, RSAKeyPair> keyPairAtIndexExtra = new HashMap<Integer, RSAKeyPair>();
      
      UTXOPool utxoPoolExtra = new UTXOPool();
      
      for (int i = 0; i < nUTXOTx; i++) {
         int num = SampleRandom.randomInt(maxUTXOTxOutput) + 1;
         Transaction tx = new Transaction();
         for (int j = 0; j < num; j++) {
            // pick a random public address
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = peopleExtra.get(rIndex).getPublicKey();
            double value = SampleRandom.randomDouble(maxValue);
            tx.addOutput(value, addr);
            keyPairAtIndexExtra.put(j, people.get(rIndex));
         }
         tx.finalize();
         // add all tx outputs to utxo pool
         for (int j = 0; j < num; j++) {
            UTXO ut = new UTXO(tx.getHash(), j);
            utxoPoolExtra.addUTXO(ut, tx.getOutput(j));
            utxoToKeyPair.put(ut, keyPairAtIndexExtra.get(j));
         }
      }
      
      ArrayList<UTXO> utxoSetExtra = utxoPoolExtra.getAllUTXO();
      int maxValidInputExtra = Math.min(maxInput, utxoSet.size() + utxoSetExtra.size());
      
      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInputExtra) + 1;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            if (Math.random() < pCorrupt) {
               UTXO utxo = utxoSetExtra.get(SampleRandom.randomInt(utxoSetExtra.size()));
               if (!utxosSeen.add(utxo)) {
                  j--;
                  continue;
               }
               tx.addInput(utxo.getTxHash(), utxo.getIndex());
               inputValue += utxoPoolExtra.getTxOutput(utxo).value;
               utxoAtIndex.put(j, utxo);
               uncorrupted = false;
            } else {
               UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
               if (!utxosSeen.add(utxo)) {
                  j--;
                  continue;
               }
               tx.addInput(utxo.getTxHash(), utxo.getIndex());
               inputValue += utxoPool.getTxOutput(utxo).value;
               utxoAtIndex.put(j, utxo);
            }
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }

   public int test6() {
      System.out.println("Test 6: test isValidTx() with transactions that claim the same UTXO multiple times");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput) + 1;
         HashSet<UTXO> utxosToRepeat = new HashSet<UTXO>();
         int indexOfUTXOToRepeat = SampleRandom.randomInt(nInput);
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            if (Math.random() < pCorrupt) {
               utxosToRepeat.add(utxo);
               uncorrupted = false;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         
         int count = 0;
         for (UTXO utxo : utxosToRepeat) {
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(nInput + count, utxo);
            count++;
         }
         
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < (nInput + utxosToRepeat.size()); j++) {
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }

   public int test7() {
      System.out.println("Test 7: test isValidTx() with transactions that contain a negative output value");

      boolean passes = true;

      for (int i = 0; i < nTxPerTest; i++) {
         Transaction tx = new Transaction();
         boolean uncorrupted = true;
         HashMap<Integer, UTXO> utxoAtIndex = new HashMap<Integer, UTXO>();
         HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
         int nInput = SampleRandom.randomInt(maxValidInput) + 1;
         double inputValue = 0;
         for (int j = 0; j < nInput; j++) {
            UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
            if (!utxosSeen.add(utxo)) {
               j--;
               continue;
            }
            tx.addInput(utxo.getTxHash(), utxo.getIndex());
            inputValue += utxoPool.getTxOutput(utxo).value;
            utxoAtIndex.put(j, utxo);
         }
         int nOutput = SampleRandom.randomInt(maxOutput) + 1;
         double outputValue = 0;
         for (int j = 0; j < nOutput; j++) {
            double value = SampleRandom.randomDouble(maxValue);
            if (outputValue + value > inputValue)
               break;
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            if (Math.random() < pCorrupt) {
               value = -value;
               uncorrupted = false;
            }
            tx.addOutput(value, addr);
            outputValue += value;
         }
         for (int j = 0; j < nInput; j++) {
            tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
         }
         tx.finalize();
         if (txHandler.isValidTx(tx) != uncorrupted) {
            passes = false;
         }
      }

      return UtilCOS.printPassFail(passes);
   }

   public static ArrayList<RSAKeyPairHelper> readKeyPairsFromFile(String filename) 
         throws FileNotFoundException, IOException {
      // Read an RSAKey from a file, return the key that was read
      FileInputStream fis = new FileInputStream(filename);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try {
         ArrayList<RSAKeyPairHelper> people = 
               new ArrayList<RSAKeyPairHelper>();
         int n = ois.readInt();
         for (int i = 0; i < n; i++) {
            BigInteger[] pub = (BigInteger[]) ois.readObject();
            BigInteger[] priv = (BigInteger[]) ois.readObject();
            int index = ois.readInt();
            RSAKey privKey = new RSAKey(priv[0], priv[1]);
            RSAKey pubKey = new RSAKey(pub[0], pub[1]);
            people.add(new RSAKeyPairHelper(pubKey, privKey));
         }
         ois.close();
         fis.close();
         return people;
      } catch(ClassNotFoundException x) {
         ois.close();
         fis.close();
         return null;
      }
   }
   
   public static void run(String[] args) throws FileNotFoundException, IOException {
      TestIsValidTx tester = new TestIsValidTx(20, 20, 20, 20, 50, 20, 20, 0.5);
      
      int total = 0;
      int numTests = 7;

      UtilCOS.printTotalNumTests(numTests);  
      total += tester.test1();
      total += tester.test2();
      total += tester.test3();
      total += tester.test4();
      total += tester.test5();
      total += tester.test6();
      total += tester.test7();

      System.out.println();
      UtilCOS.printNumTestsPassed(total, numTests);
   }  
}

public class TestTxHandler {
   private static boolean verify(Transaction[] allTxs1, UTXOPool uPool) {
      Transaction[] copyTxs1 = new Transaction[allTxs1.length];
      for (int i = 0; i < copyTxs1.length; i++)
         copyTxs1[i] = allTxs1[i];

      TxHandler student1 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

      System.out.println("Total Transactions = " + allTxs1.length);
      Transaction[] stx1 = student1.handleTxs(copyTxs1);
      System.out.println("Number of transactions returned valid by student = " + stx1.length);
      boolean passed1 = verifier1.check(allTxs1, stx1);

      return passed1;
   }
   
   private static boolean verify(Transaction[] allTxs1, Transaction[] allTxs2, UTXOPool uPool) {
      Transaction[] copyTxs1 = new Transaction[allTxs1.length];
      for (int i = 0; i < copyTxs1.length; i++)
         copyTxs1[i] = allTxs1[i];

      Transaction[] copyTxs2 = new Transaction[allTxs2.length];
      for (int i = 0; i < copyTxs2.length; i++)
         copyTxs2[i] = allTxs2[i];

      TxHandler student1 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

      TxHandler student2 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier2 = new TxHandlerVerifier(uPool);

      System.out.println("Total Transactions = " + allTxs1.length);
      Transaction[] stx1 = student1.handleTxs(copyTxs1);
      System.out.println("Number of transactions returned valid by student = " + stx1.length);
      boolean passed1 = verifier1.check(allTxs1, stx1);

      System.out.println("Total Transactions = " + allTxs2.length);
      Transaction[] stx2 = student2.handleTxs(copyTxs2);
      System.out.println("Number of transactions returned valid by student = " + stx2.length);
      boolean passed2 = verifier2.check(allTxs2, stx2);

      return passed1 && passed2;
   }
   
   private static boolean verify(Transaction[] allTxs1, Transaction[] allTxs2, 
         Transaction[] allTxs3, UTXOPool uPool) {
      Transaction[] copyTxs1 = new Transaction[allTxs1.length];
      for (int i = 0; i < copyTxs1.length; i++)
         copyTxs1[i] = allTxs1[i];

      Transaction[] copyTxs2 = new Transaction[allTxs2.length];
      for (int i = 0; i < copyTxs2.length; i++)
         copyTxs2[i] = allTxs2[i];

      Transaction[] copyTxs3 = new Transaction[allTxs3.length];
      for (int i = 0; i < copyTxs3.length; i++)
         copyTxs3[i] = allTxs3[i];

      TxHandler student1 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

      TxHandler student2 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier2 = new TxHandlerVerifier(uPool);

      TxHandler student3 = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier3 = new TxHandlerVerifier(uPool);

      System.out.println("Total Transactions = " + allTxs1.length);
      Transaction[] stx1 = student1.handleTxs(copyTxs1);
      System.out.println("Number of transactions returned valid by student = " + stx1.length);
      boolean passed1 = verifier1.check(allTxs1, stx1);

      System.out.println("Total Transactions = " + allTxs2.length);
      Transaction[] stx2 = student2.handleTxs(copyTxs2);
      System.out.println("Number of transactions returned valid by student = " + stx2.length);
      boolean passed2 = verifier2.check(allTxs2, stx2);

      System.out.println("Total Transactions = " + allTxs3.length);
      Transaction[] stx3 = student3.handleTxs(copyTxs3);
      System.out.println("Number of transactions returned valid by student = " + stx3.length);
      boolean passed3 = verifier3.check(allTxs3, stx3);

      return passed1 && passed2 && passed3;
   }

   private static boolean verifyPoolUpdate(Transaction[] allTxs1, Transaction[] allTxs2, 
         Transaction[] allTxs3, UTXOPool uPool) {
      Transaction[] copyTxs1 = new Transaction[allTxs1.length];
      for (int i = 0; i < copyTxs1.length; i++)
         copyTxs1[i] = allTxs1[i];

      Transaction[] copyTxs2 = new Transaction[allTxs2.length];
      for (int i = 0; i < copyTxs2.length; i++)
         copyTxs2[i] = allTxs2[i];

      Transaction[] copyTxs3 = new Transaction[allTxs3.length];
      for (int i = 0; i < copyTxs3.length; i++)
         copyTxs3[i] = allTxs3[i];

      TxHandler student = new TxHandler(new UTXOPool(uPool));
      TxHandlerVerifier verifier = new TxHandlerVerifier(uPool);

      System.out.println("Total Transactions = " + allTxs1.length);
      Transaction[] stx1 = student.handleTxs(copyTxs1);
      System.out.println("Number of transactions returned valid by student = " + stx1.length);
      boolean passed1 = verifier.check(allTxs1, stx1);

      System.out.println("Total Transactions = " + allTxs2.length);
      Transaction[] stx2 = student.handleTxs(copyTxs2);
      System.out.println("Number of transactions returned valid by student = " + stx2.length);
      boolean passed2 = verifier.check(allTxs2, stx2);

      System.out.println("Total Transactions = " + allTxs3.length);
      Transaction[] stx3 = student.handleTxs(copyTxs3);
      System.out.println("Number of transactions returned valid by student = " + stx3.length);
      boolean passed3 = verifier.check(allTxs3, stx3);

      return passed1 && passed2 && passed3;
   }

   // all transactions are simple and valid
   public static int test1(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 1: test handleTransactions() with simple and valid transactions");

      String common = "files/SampleTxsTest1-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test2(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 2: test handleTransactions() with simple but "
            + "some invalid transactions because of invalid signatures");

      String common = "files/SampleTxsTest2-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test3(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 3: test handleTransactions() with simple but "
            + "some invalid transactions because of inputSum < outputSum");

      String common = "files/SampleTxsTest3-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test4(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 4: test handleTransactions() with simple and "
            + "valid transactions with some double spends");

      String common = "files/SampleTxsTest4-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test5(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 5: test handleTransactions() with valid but "
            + "some transactions are simple, some depend on other transactions");

      String common = "files/SampleTxsTest5-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test6(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 6: test handleTransactions() with valid and simple but "
            + "some transactions take inputs from non-exisiting utxo's");

      String common = "files/SampleTxsTest6-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test7(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 7: test handleTransactions() with complex Transactions");

      String common = "files/SampleTxsTest7-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   // all transactions are simple and valid
   public static int test8(UTXOPool uPool) throws FileNotFoundException, IOException {
      System.out.println("Test 8: test handleTransactions() with simple, valid transactions "
            + "being called again to check for changes made in the pool");

      String common = "files/SampleTxsTest8-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verifyPoolUpdate(allTxs1, allTxs2, allTxs3, uPool));
   }

   public static void main(String[] args) throws FileNotFoundException, IOException {
	  TestIsValidTx.run(args);
      String skpFile = "files/SampleKeyPairs.txt";
      String supFile = "files/SampleUTXOPool.txt";
      SampleKeyPairs skp = SampleKeyPairsFileHandler.readKeyPairsFromFile(skpFile);
      SampleUTXOPool sup = SampleUTXOPoolFileHandler.readSampleUTXOPoolFromFile(skp, supFile);

      UTXOPool uPool = sup.getPool();
      int total = 0;
      int numTests = 8;

      UtilCOS.printTotalNumTests(numTests);  
      total += test1(uPool);
      total += test2(uPool);
      total += test3(uPool);
      total += test4(uPool);
      total += test5(uPool);
      total += test6(uPool);
      total += test7(uPool);
      total += test8(uPool);

      System.out.println();
      UtilCOS.printNumTestsPassed(total, numTests);
   }
}
