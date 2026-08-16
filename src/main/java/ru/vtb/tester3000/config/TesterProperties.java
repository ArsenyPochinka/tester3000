package ru.vtb.tester3000.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tester3000")
public class TesterProperties {

    /** Пауза между соседними сообщениями (событиями) внутри одного кейса, мс. */
    private long messageDelayMs = 2000;

    /** Сколько кейсов выполняется одновременно. */
    private int parallelTests = 2;

    /**
     * Интервал перед постановкой в очередь следующего кейса после первых {@code parallelTests}, мс.
     * Первые {@code parallelTests} стартуют сразу.
     */
    private long testStartIntervalMs = 3000;

    private final M210 m210 = new M210();
    private final Kafka kafka = new Kafka();

    public long getMessageDelayMs() {
        return messageDelayMs;
    }

    public void setMessageDelayMs(long messageDelayMs) {
        this.messageDelayMs = messageDelayMs;
    }

    public int getParallelTests() {
        return parallelTests;
    }

    public void setParallelTests(int parallelTests) {
        this.parallelTests = Math.max(1, parallelTests);
    }

    public long getTestStartIntervalMs() {
        return testStartIntervalMs;
    }

    public void setTestStartIntervalMs(long testStartIntervalMs) {
        this.testStartIntervalMs = Math.max(0, testStartIntervalMs);
    }

    public M210 getM210() {
        return m210;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public static class M210 {
        private String baseUrl = "http://127.0.0.1:8080";
        private String path = "/api/1.1.0/prod_auth/_request";
        private String mdmId = "tester3000";
        private boolean stubEnabled = true;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMdmId() {
            return mdmId;
        }

        public void setMdmId(String mdmId) {
            this.mdmId = mdmId;
        }

        public boolean isStubEnabled() {
            return stubEnabled;
        }

        public void setStubEnabled(boolean stubEnabled) {
            this.stubEnabled = stubEnabled;
        }

        public String requestUrl() {
            String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String p = path.startsWith("/") ? path : "/" + path;
            return base + p;
        }
    }

    public static class Kafka {
        private String clearingTopic = "ccop.prx.event.clearing";
        private String finOutboxTopic = "tsss.ccop_incoming_fin_message.outbox";
        private String finOutboxGroupId = "tester3000-fin-outbox";
        private String finInstructionTopic = "tsss.ccop_fin_instruction.outbox";
        private String finInstructionGroupId = "tester3000-fin-instruction";
        private String finTransactionTopic = "tsss.ccop_fin_transaction.outbox";
        private String finTransactionGroupId = "tester3000-fin-transaction";
        private boolean finOutboxStubEnabled = true;

        public String getClearingTopic() {
            return clearingTopic;
        }

        public void setClearingTopic(String clearingTopic) {
            this.clearingTopic = clearingTopic;
        }

        public String getFinOutboxTopic() {
            return finOutboxTopic;
        }

        public void setFinOutboxTopic(String finOutboxTopic) {
            this.finOutboxTopic = finOutboxTopic;
        }

        public String getFinOutboxGroupId() {
            return finOutboxGroupId;
        }

        public void setFinOutboxGroupId(String finOutboxGroupId) {
            this.finOutboxGroupId = finOutboxGroupId;
        }

        public String getFinInstructionTopic() {
            return finInstructionTopic;
        }

        public void setFinInstructionTopic(String finInstructionTopic) {
            this.finInstructionTopic = finInstructionTopic;
        }

        public String getFinInstructionGroupId() {
            return finInstructionGroupId;
        }

        public void setFinInstructionGroupId(String finInstructionGroupId) {
            this.finInstructionGroupId = finInstructionGroupId;
        }

        public String getFinTransactionTopic() {
            return finTransactionTopic;
        }

        public void setFinTransactionTopic(String finTransactionTopic) {
            this.finTransactionTopic = finTransactionTopic;
        }

        public String getFinTransactionGroupId() {
            return finTransactionGroupId;
        }

        public void setFinTransactionGroupId(String finTransactionGroupId) {
            this.finTransactionGroupId = finTransactionGroupId;
        }

        public boolean isFinOutboxStubEnabled() {
            return finOutboxStubEnabled;
        }

        public void setFinOutboxStubEnabled(boolean finOutboxStubEnabled) {
            this.finOutboxStubEnabled = finOutboxStubEnabled;
        }
    }
}
