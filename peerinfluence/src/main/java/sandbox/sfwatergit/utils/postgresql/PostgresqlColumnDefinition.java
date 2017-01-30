package sandbox.sfwatergit.utils.postgresql;

public class PostgresqlColumnDefinition {


    final String extraParams;
    final String name;
    final PostgresType type;
    public PostgresqlColumnDefinition(String name, PostgresType type,
                                      String extraParams) {
        super();
        this.name = name;
        this.type = type;
        this.extraParams = extraParams;
    }
    public PostgresqlColumnDefinition(String name, PostgresType type) {
        super();
        this.name = name;
        this.type = type;
        this.extraParams = "";
    }
}
