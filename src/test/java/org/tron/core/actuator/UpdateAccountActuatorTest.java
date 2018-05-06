package org.tron.core.actuator;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.protos.Contract;

import java.io.File;

@Slf4j
public class UpdateAccountActuatorTest {

    private static AnnotationConfigApplicationContext context;
    private static Manager dbManager;
    private static final String dbPath = "output_UpdateAccountTest";

    static {
        Args.setParam(new String[] {"--output-directory", dbPath}, Constant.TEST_CONF);
        context = new AnnotationConfigApplicationContext(DefaultConfig.class);
    }

    @BeforeClass
    public static void init() {
        dbManager = context.getBean(Manager.class);

        // create a account for test


    }

    private Any getContract(String name, String address) {
        return Any.pack(
                Contract.AccountUpdateContract.newBuilder()
                .setAccountName(ByteString.copyFromUtf8(name))
                .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(address)))
                .build()
        );
    }

    @Test
    public void rightlUpdateAccount() {

    }

    @Test
    public void addressNotExistUpdateAccount() {

    }

    @Test
    public void accountNameNullOrEmptyUpdateAccount() {

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
