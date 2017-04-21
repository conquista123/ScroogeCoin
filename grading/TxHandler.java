import java.util.*;
public class TxHandler {


	private UTXOPool utxoPool;
	/* Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is utxoPool. This should make a defensive copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);
	}


	/* Returns true if
	 * (1) all outputs claimed by tx are in the current UTXO pool,
	 * (2) the signatures on each input of tx are valid,
	 * (3) no UTXO is claimed multiple times by tx,
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of
	 its output values;
	 and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		HashSet<UTXO> seenUtxo = new HashSet<UTXO>();
		double inputVal = 0.0;
		double outputVal = 0.0;
		int index = 0;
		for(Transaction.Input in : tx.getInputs()) {
			UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
			if (!this.utxoPool.contains(ut)) { // (1)
				return false;
			}

			double prevOutVal = utxoPool.getTxOutput(ut).value;
			inputVal += prevOutVal;

			if (seenUtxo.contains(ut)) { // (3)
				return false;
			}
			seenUtxo.add(ut);

			if(!utxoPool.getTxOutput(ut).address.verifySignature(tx.getRawDataToSign(index), in.signature)) { // (2)
				return false;
			}
			index++;

		}
		for (Transaction.Output out : tx.getOutputs()) {
			if (out.value < 0.0) { // (4)
				return false;
			}
			outputVal += out.value;
		}
		if (outputVal > inputVal) { // (5)
			return false;
		}
		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// (1) Return only valid transactions
		// (2) One transaction's inputs may depend on the output of another
		// transaction in the same epoch
		// (3) Update uxtoPool
		// (4) Return mutally valid transaction set of maximal size

		HashSet<Transaction> txs = new HashSet<Transaction>(Arrays.asList(possibleTxs));
		int txCount = 0;
		ArrayList<Transaction> valid = new ArrayList<Transaction>();

		do {
			txCount = txs.size();
			HashSet<Transaction> toRemove = new HashSet<Transaction>();
			for (Transaction tx : txs) {
				if(!isValidTx(tx)) {
					continue;
				}

				valid.add(tx);
				updatePool(tx);
				toRemove.add(tx);

			}

			for (Transaction tx : toRemove){
				txs.remove(tx);
			}


		} while (txCount != txs.size()  && txCount != 0);
		return valid.toArray(new Transaction[valid.size()]);
	}

	private void updatePool(Transaction tx){

		for(Transaction.Input input : tx.getInputs()) {
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
			this.utxoPool.removeUTXO(utxo);
		}

		byte[] txHash = tx.getHash();
		int index = 0;
		for (Transaction.Output output : tx.getOutputs()) {
			UTXO utxo = new UTXO(txHash, index);
			index++;
			this.utxoPool.addUTXO(utxo,output);
		}
	}
}
