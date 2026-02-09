package com.tomkeuper.bedwars.history;

import java.util.List;

public class MatchHistoryPage {
    private final List<MatchHistoryRecord> records;
    private final int totalCount;

    public MatchHistoryPage(List<MatchHistoryRecord> records, int totalCount) {
        this.records = records;
        this.totalCount = totalCount;
    }

    public List<MatchHistoryRecord> getRecords() {
        return records;
    }

    public int getTotalCount() {
        return totalCount;
    }
}

