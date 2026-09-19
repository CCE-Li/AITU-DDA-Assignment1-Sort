package kz.aitu.daa.benchmark;

/** The three input shapes required by the assignment. */
public enum InputType {

    RANDOM("random"),
    SORTED("sorted"),
    DUPLICATES("duplicates");

    private final String csvName;

    InputType(String csvName) {
        this.csvName = csvName;
    }

    public String csvName() {
        return csvName;
    }
}
