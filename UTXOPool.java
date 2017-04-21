/* 
 * UTXOPool.java
 * 
 * This class represents a UTXO pool, which is a mapping from UTXOs
 * to their corresponding transction outputs
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UTXOPool {
   
   // The current collection of UTXOs, with each one mapped to its corresponding 
   // transaction output
   private HashMap<UTXO, Transaction.Output> H;
   
   // Creates a new empty UTXOPool
   public UTXOPool() {
      H = new HashMap<UTXO, Transaction.Output>();
   }
   
   // Creates a new UTXOPool that is a copy of <uPool>
   public UTXOPool(UTXOPool uPool) {
      H = new HashMap<UTXO, Transaction.Output>(uPool.H);
   }
   
   // Adds a mapping from UTXO <utxo> to transaction output <txOut> to the pool
   public void addUTXO(UTXO utxo, Transaction.Output txOut) {
      H.put(utxo, txOut);
   }
   
   // Removes the UTXO <utxo> from the pool 
   public void removeUTXO(UTXO utxo) {
      H.remove(utxo);
   }
   
   // Returns the transaction output corresponding to UTXO <utxo>, or null if 
   // <utxo> is not in the pool.
   public Transaction.Output getTxOutput(UTXO ut) {
      return H.get(ut);
   }
   
   // Returns true if UTXO <utxo> is in the pool and false otherwise
   public boolean contains(UTXO utxo) {
      return H.containsKey(utxo);
   }
   
   // Returns an ArrayList of all UTXOs in the pool
   public ArrayList<UTXO> getAllUTXO() {
      Set<UTXO> setUTXO = H.keySet();
      ArrayList<UTXO> allUTXO = new ArrayList<UTXO>();
      for (UTXO ut : setUTXO) {
         allUTXO.add(ut);
      }
      return allUTXO;
   }
}
