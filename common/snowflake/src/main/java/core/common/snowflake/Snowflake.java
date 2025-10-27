package core.common.snowflake;

import java.util.random.RandomGenerator;

public class Snowflake {
	private static final int NODE_ID_BITS = 10;
	private static final int SEQUENCE_BITS = 12;

	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

	private final long nodeId = RandomGenerator.getDefault().nextLong(maxNodeId + 1);// node id는 인스턴스 별 할당받는 것이 좋음
	// UTC = 2024-01-01T00:00:00Z
	private static final long startTimeMillis = 1704067200000L;

	private long lastTimeMillis = startTimeMillis;
	private long sequence = 0L;

	private Snowflake() { }

	private static final Snowflake snowflake = new Snowflake();

    public static Snowflake getInstance(){
		return snowflake;
	}

	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		if (currentTimeMillis == lastTimeMillis) { // 같은 millisecond 내에서 여러 번 호출된 경우
			sequence = (sequence + 1) & maxSequence; // sequence 값을 증가시켜 ID를 구분함
			if (sequence == 0) { // sequence가 0이면 최대치를 사용한 것이므로 기다림
				currentTimeMillis = waitNextMillis(currentTimeMillis);
			}
		} else {
			sequence = 0;
		}

		lastTimeMillis = currentTimeMillis;

		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS))
			| (nodeId << SEQUENCE_BITS)
			| sequence;
	}

	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis();
		}
		return currentTimestamp;
	}
}
