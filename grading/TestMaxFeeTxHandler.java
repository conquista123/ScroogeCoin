import java.io.FileNotFoundException;
import java.io.IOException;

public class TestMaxFeeTxHandler {
   private static boolean verify(Transaction[] allTxs1, UTXOPool uPool) {
      Transaction[] copyTxs1 = new Transaction[allTxs1.length];
      for (int i = 0; i < copyTxs1.length; i++)
         copyTxs1[i] = allTxs1[i];

      MaxFeeTxHandler student1 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier1 = new MaxFeeTxHandlerVerifier(uPool);

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

      MaxFeeTxHandler student1 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier1 = new MaxFeeTxHandlerVerifier(uPool);

      MaxFeeTxHandler student2 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier2 = new MaxFeeTxHandlerVerifier(uPool);

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

      MaxFeeTxHandler student1 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier1 = new MaxFeeTxHandlerVerifier(uPool);

      MaxFeeTxHandler student2 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier2 = new MaxFeeTxHandlerVerifier(uPool);

      MaxFeeTxHandler student3 = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier3 = new MaxFeeTxHandlerVerifier(uPool);

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

      MaxFeeTxHandler student = new MaxFeeTxHandler(new UTXOPool(uPool));
      MaxFeeTxHandlerVerifier verifier = new MaxFeeTxHandlerVerifier(uPool);

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

      String common = "files/SampleMaxFeeTxsTest1-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
      Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
      Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

      return UtilCOS.printPassFail(verify(allTxs1, allTxs2, allTxs3, uPool));
   }

   public static void main(String[] args) throws FileNotFoundException, IOException {
      String skpFile = "files/SampleMaxFeeKeyPairs.txt";
      String supFile = "files/SampleMaxFeeUTXOPool.txt";
      SampleKeyPairs skp = SampleKeyPairsFileHandler.readKeyPairsFromFile(skpFile);
      SampleUTXOPool sup = SampleUTXOPoolFileHandler.readSampleUTXOPoolFromFile(skp, supFile);

      UTXOPool uPool = sup.getPool();
      int total = 0;
      int numTests = 1;

      UtilCOS.printTotalNumTests(numTests);  
      total += test1(uPool);

      System.out.println();
      UtilCOS.printNumTestsPassed(total, numTests);
   }
}
