package com.bosu.housebook.imports;

import java.io.InputStream;
import java.util.List;

public interface StatementParser {

    ImportProvider provider();

    List<ParsedTransaction> parse(InputStream inputStream);
}
