package `in`.indianrail.ncr.enireekshan

import be.quodlibet.boxable.BaseTable

interface TableWriterInterface {
    fun writeReportToPDF():String;
    fun writeHeaderToPDF(baseTable: BaseTable) : BaseTable;
    fun writeTableToPDF(baseTable: BaseTable) : BaseTable;
    fun writeTableToPDF(baseTable: BaseTable, index: Int) : BaseTable;
}