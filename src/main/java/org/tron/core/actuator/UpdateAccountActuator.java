package org.tron.core.actuator;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.StringUtil;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.db.AccountStore;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AccountUpdateContract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction.Result.code;


import java.util.Iterator;

@Slf4j
public class UpdateAccountActuator extends AbstractActuator {

  UpdateAccountActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule result) throws ContractExeException {
    long fee = calcFee();
    try {

      AccountUpdateContract accountUpdateContract = contract.unpack(AccountUpdateContract.class);
      AccountCapsule account =
          dbManager.getAccountStore().get(accountUpdateContract.getOwnerAddress().toByteArray());

      account.setAccountName(accountUpdateContract.getAccountName().toByteArray());
      dbManager.getAccountStore().put(accountUpdateContract.getOwnerAddress().toByteArray(),
          account);
      result.setStatus(fee, code.SUCESS);
      return true;
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      result.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
  }

  @Override
  public boolean validate() throws ContractValidateException {
    try {
      if (!contract.is(Contract.AccountUpdateContract.class)) {
        throw new ContractValidateException(
                "contract type error, expected type [AccountUpdateContract], real type[" + contract
                        .getClass() + "]");
      }

      Contract.AccountUpdateContract contract = this.contract.unpack(Contract.AccountUpdateContract.class);
      if (!Wallet.addressValid(contract.getOwnerAddress().toByteArray())) {
        throw new ContractValidateException("Invalidate address");
      }

      String readableOwnerAddress = StringUtil.createReadableString(contract.getOwnerAddress());

      if (!dbManager.getAccountStore().has(contract.getOwnerAddress().toByteArray())) {
        throw new ContractValidateException(
                "Account[" + readableOwnerAddress + "] not exists");
      }

      String readableAccountName = contract.getAccountName().toString();
      if (readableAccountName == null || readableAccountName.isEmpty()) {
        throw new ContractValidateException(
                "Account Name is empty"
        );
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ContractValidateException(ex.getMessage());
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(AccountUpdateContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}
