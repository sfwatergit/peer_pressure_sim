package sandbox.sfwatergit.utils.postgresql;

public enum PostgresType {
    FLOAT8(128), INT(64), TEXT(1000), BOOLEAN(8), BIGINT(64);
    private final int size;

    PostgresType(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }
}
