package com.fitpay.android.api.models.card;

import com.fitpay.android.TestActions;
import com.fitpay.android.TestConstants;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.CommitConfirm;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.utils.NamedResource;

import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tgs on 5/31/16.
 */
public class CommitTest2 extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(CommitTest2.class);

    @Test
    public void testCanConfirmCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        for (Commit commit : commits.getResults()) {
            final CountDownLatch latch = new CountDownLatch(1);

            if (commit.canConfirmCommit()) {
                CommitConfirm confirm = new CommitConfirm(ResponseState.SUCCESS);

                commit.confirm(confirm, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        fail("commit confirm failed");
                        latch.countDown();
                    }
                });
            } else {
                // TODO: uncomment out once the paltform supports this feature fully
//                if (!commit.getCommitType().equals(CommitTypes.APDU_PACKAGE)) {
//                    fail("expected confirm link on commit: " + commit);
//                }
                latch.countDown();
            }

            latch.await(5000, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void testCanGetCommitsAfter() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        for (Commit commit : commits.getResults()) {
            Collections.CommitsCollection lastCommits = getCommits(createdDevice, commit.getCommitId());
            assertEquals("number of commits with lastId", --totalResults, lastCommits.getTotalResults());
        }
    }

    @Test
    public void testCanGetCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + totalResults, totalResults >= 2);
    }

    @Test
    public void testCanGetAllCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        commits = getAllCommits(createdDevice, null);
        assertNotNull("allCommits collection", commits);
        assertEquals("number of allCommits", totalResults, commits.getTotalResults());

        for (Commit commit : commits.getResults()) {
            Collections.CommitsCollection lastCommits = getAllCommits(createdDevice, commit.getCommitId());
            assertEquals("number of commits with lastId", --totalResults, lastCommits.getTotalResults());
        }
    }

    @Test
    public void testCanGetLofOfCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        CreditCard[] creditCardArray = new CreditCard[8];
        String panBase = "99995044545454";
        int count = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                String pan = panBase + i + j;
                CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
                CreditCard createdCard = createCreditCard(user, creditCardInfo);
                assertNotNull("card not created", createdCard);

                creditCardArray[count] = acceptTerms(createdCard);
                count++;

            }
        }

        int activeAndPending = 0;

        for (int i = 0; i < 6; i++) {
            activeAndPending = 0;

            for (CreditCard card : creditCardArray) {
                CreditCard updateCard = getCreditCard(card);
                if ("ACTIVE".equals(updateCard.state)) {
                    activeAndPending++;
                }
            }

            if(activeAndPending == creditCardArray.length){
                break;
            }

            TestConstants.waitForAction(10000);
        }

        assertEquals("number of accepted cards should be 8. Got: " + activeAndPending, activeAndPending, creditCardArray.length);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < count; j++) {
                if (creditCardArray[j].canMakeDefault()) {
                    makeDefaultCard(creditCardArray[j]);
                }
            }
        }

        //wait for all commits
        final int correctResult = 25; //WARNING: it could be changed in the future
        int totalResults = 0;

        for (int i = 0; i < 6; i++) {
            Collections.CommitsCollection commits = getAllCommits(createdDevice, null);
            assertNotNull(commits);
            assertTrue("number of commits should be 10 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 10);

            totalResults = commits.getTotalResults();

            if(totalResults >= correctResult){
                break;
            }

            TestConstants.waitForAction();
        }

        assertTrue("number of commits should be > 25.  Got: " + totalResults, totalResults >= correctResult);
    }
}
