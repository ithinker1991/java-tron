package org.tron.core.actuator;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Transaction.Result.code;

import java.io.File;

@Slf4j
public class UpdateAccountActuatorTest {

    private static AnnotationConfigApplicationContext context;
    private static Manager dbManager;
    private static final String dbPath = "output_UpdateAccountTest";

    private static final String ACCOUNT_NAME_EXIST = "account_exist";
    private static final String ACCOUNT_NAME_UPDATED = "account_updated";
    private static final String ACCOUNT_NAME_EMPTY = "";

    private static final String OWNER_ADDRESS_EXIST =
            Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049abc";
    private static final String OWNER_ADDRESS_NOT_EXIST =
            Wallet.getAddressPreFixString() + "444449367799eaa3197fec1144eb71de1e049abc";


    static {
        Args.setParam(new String[] {"--output-directory", dbPath}, Constant.TEST_CONF);
        context = new AnnotationConfigApplicationContext(DefaultConfig.class);
    }

    @BeforeClass
    public static void init() {
        dbManager = context.getBean(Manager.class);

        // create a account for test
        CreateAccountActuator createAccountActuator =
                new CreateAccountActuator(getCreateAccountContract(ACCOUNT_NAME_EXIST, OWNER_ADDRESS_EXIST), dbManager);
        TransactionResultCapsule createAccountRet = new TransactionResultCapsule();
        try {
            createAccountActuator.execute(createAccountRet);
        } catch (ContractExeException e) {
            e.printStackTrace();
        }



    }

    private static Any getUpdateAccountContract(String name, String address) {
        return Any.pack(
                Contract.AccountUpdateContract.newBuilder()
                .setAccountName(ByteString.copyFromUtf8(name))
                .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(address)))
                .build()
        );
    }


    private static Any getCreateAccountContract(String name, String address) {
        return Any.pack(
                Contract.AccountCreateContract.newBuilder()
                        .setAccountName(ByteString.copyFromUtf8(name))
                        .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(address)))
                        .build());
    }

    @Test
    public void rightlUpdateAccount() {
        // update account
        UpdateAccountActuator actuator =
                new UpdateAccountActuator(getUpdateAccountContract(ACCOUNT_NAME_UPDATED, OWNER_ADDRESS_EXIST), dbManager);
        TransactionResultCapsule ret = new TransactionResultCapsule();

        try {
            actuator.validate();
            actuator.execute(ret);

            AccountCapsule owner = dbManager.getAccountStore().get(ByteArray.fromHexString(OWNER_ADDRESS_EXIST));
            Assert.assertEquals(ACCOUNT_NAME_UPDATED, owner.getInstance().getAccountName().toStringUtf8());
            Assert.assertEquals(code.SUCESS, ret.getInstance().getRet());

        } catch (ContractExeException e) {
            Assert.assertFalse(e instanceof ContractExeException);
        } catch (ContractValidateException e) {
            Assert.assertFalse(e instanceof ContractValidateException);
        }
    }

    @Test
    public void addressNotExistUpdateAccount() {
        UpdateAccountActuator actuator =
                new UpdateAccountActuator(getUpdateAccountContract(ACCOUNT_NAME_UPDATED, OWNER_ADDRESS_NOT_EXIST), dbManager);
        TransactionResultCapsule ret = new TransactionResultCapsule();

        try {
            actuator.validate();
            actuator.execute(ret);
        }  catch (ContractValidateException e) {
            Assert.assertTrue(e instanceof ContractValidateException);
            Assert.assertEquals(e.getMessage(), "Account[" + OWNER_ADDRESS_NOT_EXIST + "] not exists");
        } catch (ContractExeException e) {
            Assert.assertFalse(e instanceof ContractExeException);
        }
    }

    @Test
    public void accountNameNullOrEmptyUpdateAccount() {
        UpdateAccountActuator actuator =
                new UpdateAccountActuator(getUpdateAccountContract(ACCOUNT_NAME_EMPTY, OWNER_ADDRESS_EXIST), dbManager);
        TransactionResultCapsule ret = new TransactionResultCapsule();

        try {
            actuator.validate();
            actuator.execute(ret);
        }  catch (ContractValidateException e) {
            Assert.assertTrue(e instanceof ContractValidateException);
            Assert.assertEquals(e.getMessage(), "Account Name is empty");
        } catch (ContractExeException e) {
            Assert.assertTrue(e instanceof ContractExeException);
        }

    }


    /** Release resources. */
    @AfterClass
    public static void destroy() {
        Args.clearParam();
        if (FileUtil.deleteDir(new File(dbPath))) {
            logger.info("Release resources successful.");
        } else {
            logger.info("Release resources failure.");
        }
        context.destroy();
    }
}
