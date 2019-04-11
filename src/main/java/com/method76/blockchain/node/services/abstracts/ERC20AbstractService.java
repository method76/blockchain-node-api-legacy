package com.method76.blockchain.node.services.abstracts;

import com.method76.blockchain.node.constants.EthereumConstant;
import com.method76.blockchain.node.domain.TbAddressBalance;
import com.method76.blockchain.node.domain.TbBlockchainMaster;
import com.method76.blockchain.node.domain.TbTrans;
import com.method76.blockchain.node.domain.primarykeys.PkSymbolAddr;
import com.method76.blockchain.node.gsonObjects.BitcoinStringResponse;
import com.method76.blockchain.node.gsonObjects.NewAddressResponse;
import com.method76.blockchain.node.gsonObjects.PersonalInfoRequest;
import com.method76.blockchain.node.gsonObjects.ValidateAddressResponse;
import com.method76.blockchain.node.services.interfaces.LockableAddress;
import com.method76.blockchain.node.utils.JsonRpcUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ETH services
 * 
 * @author sungjoon.kim
 */
@Slf4j public abstract class ERC20AbstractService extends BlockchainRpcService
			implements EthereumConstant, LockableAddress {

	public abstract String getContractaddr();
	private BigInteger gasPrice;
	
	public EthereumAbstractService getPlatformService() {
//		return (EthereumAbstractService) coinFactory.getService(SYMBOL_ETH);
		return null;
	}
	public Web3j getWeb3j() { return getPlatformService().getWeb3j(); }
	
	@Override public String getRpcurl() {
		return getPlatformService().getRpcurl();
	}
	@Override public String getPp() {
		return getPlatformService().getPp();
	}
	@Override public String getSendaddr() {
		return getPlatformService().getSendaddr();
	}
    @Override public boolean isSendAddrExists() {
    	return getPlatformService().isSendAddrExists();
    }
	@Override public Object getTransaction(String txid) {
		return getPlatformService().getTransaction(txid);
	}
	@Transactional
    @Override public void beforeBatchSend() {
		gasPrice = getPlatformService().getGasPrice();
	}
	
	@Override public boolean syncWalletBalance(int uid) { return false; }
	
	@Transactional
    @Override public boolean syncWalletBalances() {

        List<String> list = getPlatformService().getAllAddressListFromNode();
        if (list==null || list.size()<1) { return true; }
        
        String senderaddr = getSendaddr();
        double totalbal = 0, senderbal = 0;
        int successcount = 0;
        int totalcount = list.size();
        
		for (String addr : list) {
            
			double actualbal = getAddressBalance(addr);
            totalbal += actualbal;
            if (addr.equals(senderaddr)) { senderbal = actualbal; }
            
			List<TbAddressBalance> data = addrBalanceRepo.findBySymbolAndAddr(getSymbol(), addr);
			if (data!=null && data.size()>0) {
            	if (data.size()>1) {
            		logError("findBySymbolAndAddr", "this should not be happened");
            	}
				TbAddressBalance datum = data.get(0); 
				datum.setBalance(actualbal);
				addrBalanceRepo.save(datum);
			} else if ((data==null || data.size()<1) && actualbal>0) {
				PkSymbolAddr id = new PkSymbolAddr(getSymbol(), addr);
//				Optional<TbOrphanAddress> oraw = orphanAddrRepo.findById(id);
//				TbOrphanAddress odatum = null;
//				if (oraw.isPresent()) {
//					odatum = oraw.get();
//					odatum.setBalance(actualbal);
//					orphanAddrRepo.save(odatum);
//				} else {
//					odatum = new TbOrphanAddress(id);
//					odatum.setBalance(actualbal);
//					orphanAddrRepo.save(odatum);
//				}
			}
			successcount++;
		}
		return (totalcount == successcount);
	}

	@Override public boolean sendOneTransaction(TbTrans datum) {
		logInfo("SendOneTransaction", "Started");
		try {
			String fromaddr = (datum.getFromAddr()!=null && datum.getFromAddr().length() > 3) 
					? datum.getFromAddr():getSendaddr();
			String method = (datum.getFromAddr()==null||datum.getFromAddr().length()>3)
					?ERC_TRANSFER_CODE:ERC_TRANSFERFROM_CODE;
			String txId = transferWithDataField(method, fromaddr, datum.getToAddr(), 
					datum.getAmount());

			if (txId != null) {
				datum.setTxid(txId);
				if (ERC_TRANSFER_CODE.equals(method)) {
					logSuccess(ERC_TRANSFER_NAME, "to " + datum.getToAddr() + " amount "
							+ datum.getAmount() + " txid " + txId);
				} else if (ERC_TRANSFERFROM_CODE.equals(method)) {
					logSuccess(ERC_TRANSFERFROM_NAME, "from " + datum.getFromAddr() + " to " + datum.getToAddr() + " amount "
							+ datum.getAmount() + " txid " + txId);
				}
				return true;
			} else {
				datum.setErrMsg("[-1] failed to call transfer contract");
				return false;
			}

		} catch (Exception e) {
			datum.setErrMsg("[-1] " + e.getMessage());
			e.printStackTrace();
			logError(ERC_TRANSFER_NAME, e);
			return false;
		}
	}

	/**
	 * Function constructor arg0: Function name Function constructor arg1:
	 * Parameters to pass as Solidity Types
	 * @param toaddr
	 * @param rawamount
	 * @return
	 * @throws Exception
	 */
	public String transfer(String toaddr, double rawamount) throws Exception {
		return transferWithDataField(ERC_TRANSFER_CODE, getSendaddr(), toaddr, rawamount);
	}

	public String transferFrom(String fromaddr, String toaddr, double rawamount) throws Exception {
		return transferWithDataField(ERC_TRANSFERFROM_CODE, fromaddr, toaddr, rawamount);
	}

	private String transferWithDataField(String method, String fromaddr, String toaddr, double rawAmount) throws IOException {
		logInfo("transferWithDataField", "Started");
		if (toaddr == null) { return null; }
		try {
			String dataField[] = new String[3];
			
			// contract method: "0xa9059cbb"(transfer) or "0x23b872dd"(transferFrom)
			dataField[0] = method;

			// to address
			dataField[1] = String.format("%64s", toaddr.substring(2)).replace(' ', '0');
			
			// token amount
			String tokenAmtHex = JsonRpcUtil.tokenAmountToHex(rawAmount, getDecimals());
			dataField[2] = String.format("%64s", tokenAmtHex).replace(' ', '0');

			String data = dataField[0] + dataField[1] + dataField[2];
			logInfo("transferWithDataField data field:", data);

			JSONObject params = new JSONObject();
			params.put("from",  	fromaddr);
			params.put("to",    	getContractaddr());
			params.put("gas",   	Numeric.toHexStringWithPrefix(getGasLimit())); // ���� WEI
			params.put("lastGasPrice", 	Numeric.toHexStringWithPrefix(
					JsonRpcUtil.bigIntegerAddFirst(gasPrice))); // ���� WEI
			params.put("value", 	"0x0"); // Only sending token, not eth
			params.put("data",  	data);
			
			JSONArray paramArr = new JSONArray();
			paramArr.put(params);

			String resStr = JsonRpcUtil.sendJsonRpcJson(getRpcurl(), METHOD_SENDFROMADDR,
					paramArr);
			BitcoinStringResponse res = gson.fromJson(resStr, BitcoinStringResponse.class);
			String txId = res.getResult();
			logInfo("transferWithDataField txid ", txId);
			return txId;
		} catch (Exception e) {
			logError("transferWithDataField", e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Ex) params {"jsonrpc":"2.0","method":"eth_call", "params":[{"to":
	 * "0xee74110fb5a1007b06282e0de5d73a61bf41d9cd",
	 * "data":"0x70a08231000000000000000000000000c9fbb1691def4d2a54a3eb22e0164359628aa98b"},
	 * "latest"], "id":67}
	 */
	@Override public double getAddressBalance(String addr) {
		if (addr == null) { return 0; }
		try {
			Function function = new Function(METHOD_ERC_BALANCEOF, 
					Collections.singletonList(new Address(addr)),
					Collections.singletonList(new TypeReference<Uint256>() {
					}));
//			log.debug(getSendaddr() + " " + getContractaddr());
			String resStr = callSmartContract(function, getSendaddr(), getContractaddr());
//			log.debug("getAddressBalance " + resStr);
			List<Type> res = FunctionReturnDecoder.decode(resStr, function.getOutputParameters());
			return (double) new BigDecimal((BigInteger) res.get(0).getValue())
					.divide(BigDecimal.TEN.pow(getDecimals()))
					.doubleValue();
		} catch (Exception e) {
			logError("getAddressBalance", e);
			e.printStackTrace();
			return 0;
		}

	}

	/**
	 * arg0 = caller addr
	 * @param function
	 * @param fromAddr
	 * @param contractAddress
	 * @return
	 * @throws Exception
	 */
	private String callSmartContract(Function function, String fromAddr, 
			String contractAddress) throws Exception {
		String encodedFunction = FunctionEncoder.encode(function);
		try {
			EthCall response = getWeb3j()
					.ethCall(Transaction.createEthCallTransaction(fromAddr, contractAddress, 
							encodedFunction), DefaultBlockParameterName.LATEST)
					.sendAsync().get(); // ERROR - java.util.concurrent.ExecutionException: java.net.ConnectException: Failed to connect to
			return response.getValue();
		} catch(Exception e) {
			logError("callSmartContract", e);
			return null;
		}
	}
	/**
	 */
	@Transactional
    @Override public boolean updateTxConfirmCount() {
		return getPlatformService().updateSendConfirm(getSymbol());
	}

	@Transactional
    @Override public boolean updateReceiveConfirm() {
		return getPlatformService().updateReceiveConfirm(getSymbol());
	}

	/**
	 * Transfer.GAS_LIMIT or BigInteger.valueOf(90000);
	 * @return
	 */
	public BigInteger getGasLimit() {
		return BigInteger.valueOf(200000);
	}

	@Override public ValidateAddressResponse validateAddress(PersonalInfoRequest param) {
		return getPlatformService().validateAddress(param);
	}
	
	@Transactional
    @Override public NewAddressResponse newAddress(PersonalInfoRequest req) {
		NewAddressResponse prev = getSavedAddress(req);
		if (prev.getResult().getAddress() != null) {
			return prev;
		}
		NewAddressResponse ret = getPlatformService().newAddress(req);
		TbAddressBalance datum = new TbAddressBalance(
				ret.getResult().getUid(), getSymbol(),
				null, ret.getResult().getAddress(), null,
				ret.getResult().getBrokerId());
		addrBalanceRepo.save(datum);
		return ret;
	}

	@Override public boolean walletpassphraseWithAddress(String address) {
		return ((LockableAddress) getPlatformService()).walletpassphraseWithAddress(address);
	}

	@Override public boolean walletlock(String address) {
		return ((LockableAddress) getPlatformService()).walletlock(address);
	}

}
