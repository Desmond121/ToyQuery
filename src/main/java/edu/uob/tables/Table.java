package edu.uob.tables;

import edu.uob.exceptions.TableException;

import java.util.*;
import java.util.function.BiPredicate;


public class Table {
    private final static String TABLE_SEPARATOR = "\t";
    private final HashMap<String, Integer> attributesMap;
    private final TreeMap<Integer, ArrayList<String>> records;
    private int lastId;

    public Table() {
        lastId = 0;
        attributesMap = new HashMap<>();
        records = new TreeMap<>();
    }

    public Table(String tableString) throws TableException {
        this();
        fromString(tableString);
    }

    private int getIndexOfAttribute(String attributeName) throws TableException {
        if (!attributesMap.containsKey(attributeName)) {
            throw new TableException.AttributeMissingException(attributeName);
        }
        return attributesMap.get(attributeName);
    }

    // if predicate return true then include that id.
    public Set<Integer> getIdsByMethod(String attribute, Value referenceValue, BiPredicate<Value, Value> compareMethod) throws TableException {
        Set<Integer> ids = new TreeSet<>(); // result set
        // if attribute is id.
        for (Map.Entry<Integer, ArrayList<String>> record : records.entrySet()) {
            String valueString = attribute.equals("id") ? //
                    record.getKey().toString() : //
                    record.getValue().get(getIndexOfAttribute(attribute));
            if (compareMethod.test(new Value(valueString, referenceValue.type()),
                                   referenceValue)) {
                ids.add(record.getKey()); // add id to result set
            }
        }
        return ids;
    }

    public Set<Integer> getIds() {
        return records.keySet();
    }

    // insert record with provided id.
    public void insertRecord(List<String> record, int id) throws TableException {
        // attributes of a record should be matched. 'id' not included.
        if (records.containsKey(id)) {
            throw new TableException.InvalidTableOperationException(
                    "Duplicate primary key.");
        }
        if (record.size() != attributesMap.size()) {
            throw new TableException.InvalidTableOperationException(
                    attributesMap.size() + " value(s) expected but " + record.size() +
                    " value(s) inserted.");
        }
        records.put(id, new ArrayList<>(record));
        // update lastId.
        this.lastId = Math.max(id, this.lastId);
    }

    // insert record with generated id.
    public void insertRecord(List<String> record) throws TableException {
        insertRecord(record, ++lastId);
    }

    public void deleteRecord(int id) throws TableException {
        if (!records.containsKey(id))
            throw new TableException.InvalidTableOperationException("Id not exist.");
        records.remove(id);
    }

    public void addAttribute(String name) throws TableException {
        // already existing.
        if (attributesMap.containsKey(name) || name.equals("id")) {
            throw new TableException.AttributeDuplicatedException(name);
        }
        attributesMap.put(name, attributesMap.size());
        for (ArrayList<String> record : records.values()) {
            // new attribute created but no value put in yet.
            record.add("NULL");
        }
    }

    public void dropAttribute(String name) throws TableException {
        // cannot drop id
        if (name.equals("id")) {
            throw new TableException.InvalidTableOperationException(
                    "Cannot drop primary key.");
        }
        // not existing.
        if (!attributesMap.containsKey(name)) {
            throw new TableException.AttributeMissingException(name);
        }
        // remove this column in all records.
        int column = attributesMap.remove(name);
        for (ArrayList<String> record : records.values()) {
            record.remove(column);
        }
    }

    public void setValue(String attribute, String value, int id) throws TableException {
        if (attribute.equals("id")) {
            throw new TableException.InvalidTableOperationException(
                    "Cannot update primary key.");
        }
        if (!records.containsKey(id)) throw new TableException.IdNotFoundException(id);
        records.get(id).set(getIndexOfAttribute(attribute), value);
    }

    public String getValue(String attribute, int id) throws TableException {
        String value;
        if (!records.containsKey(id)) throw new TableException.IdNotFoundException(id);
        if (attribute.equals("id")) value = "" + id;
        else value = records.get(id).get(getIndexOfAttribute(attribute));
        return value;
    }

    public List<String> getValues(int id) throws TableException {
        if (!records.containsKey(id)) throw new TableException.IdNotFoundException(id);
        return new ArrayList<>(records.get(id));
    }

    public List<String> getAttributeList() {
        // make sure attributes are in correct sequence.
        TreeMap<Integer, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Integer> pair : attributesMap.entrySet()) {
            sortedMap.put(pair.getValue(), pair.getKey());
        }
        ArrayList<String> result = new ArrayList<>();
        result.add("id");
        result.addAll(sortedMap.values());
        return result;
    }

    private String generateRecordsString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, ArrayList<String>> record : records.entrySet()) {
            result.append(record.getKey());
            for (String value : record.getValue()) {
                result.append(TABLE_SEPARATOR).append(value);
            }
            result.append('\n');
        }
        return result.toString();
    }

    private String generateAttributesString() {
        // make sure attributes are in correct sequence.
        TreeMap<Integer, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Integer> pair : attributesMap.entrySet()) {
            sortedMap.put(pair.getValue(), pair.getKey());
        }
        // append string
        StringBuilder result = new StringBuilder("id");
        for (String attribute : sortedMap.values()) {
            result.append(TABLE_SEPARATOR).append(attribute);
        }
        return result.append('\n').toString();
    }

    @Override
    public String toString() {
        return generateAttributesString() + generateRecordsString();
    }

    private String generateAttributeStringNoId() {
        // make sure attributes are in correct sequence.
        TreeMap<Integer, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Integer> pair : attributesMap.entrySet()) {
            sortedMap.put(pair.getValue(), pair.getKey());
        }
        // append string
        StringBuilder result = new StringBuilder();
        for (String attribute : sortedMap.values()) {
            result.append(attribute).append(TABLE_SEPARATOR);
        }
        return result.deleteCharAt(result.length() - 1).append("\n").toString();
    }

    private String generateRecordsStringNoId() {
        StringBuilder result = new StringBuilder();
        for (ArrayList<String> record : records.values()) {
            for (String value : record) {
                result.append(value).append(TABLE_SEPARATOR);
            }
            result.deleteCharAt(result.length() - 1).append("\n");
        }
        return result.toString();
    }

    public String toStringNoId() {
        return generateAttributeStringNoId() + generateRecordsStringNoId();
    }

    private void readRecordsFormLine(String recordsLine) throws TableException {
        ArrayList<String> values = new ArrayList<>(
                Arrays.asList(recordsLine.split(TABLE_SEPARATOR)));
        // first value should be id.
        int id = Integer.parseInt(values.remove(0));
        // insert values. check duplicated id first.
        if (records.containsKey(id)) {
            throw new TableException.InvalidImportStringException(
                    "Duplicated key: " + id + ".");
        }
        insertRecord(values, id);
    }

    private void readAttributeFromLine(String attributesLine) throws TableException {
        ArrayList<String> attributes = new ArrayList<>(
                Arrays.asList(attributesLine.split(TABLE_SEPARATOR)));
        // remove first attribute which is id.
        if (attributes.remove(0).equals("id")) {
            for (String attribute : attributes) {
                addAttribute(attribute);
            }
        }
        else {
            throw new TableException.InvalidImportStringException("Missing primary key.");
        }
    }

    private void fromString(String tableString) throws TableException {
        // erase all data first.
        attributesMap.clear();
        records.clear();
        lastId = 0;
        // trim all trailing line or space
        Scanner scanner = new Scanner(tableString.trim());
        // get first line as attributes.
        if (scanner.hasNextLine()) {
            readAttributeFromLine(scanner.nextLine());
        }
        // other lines are records.
        while (scanner.hasNextLine()) {
            readRecordsFormLine(scanner.nextLine());
        }
    }
}

