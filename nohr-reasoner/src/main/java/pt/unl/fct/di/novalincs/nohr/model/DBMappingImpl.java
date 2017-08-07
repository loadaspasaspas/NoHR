package pt.unl.fct.di.novalincs.nohr.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link Rule}.
 *
 * @author Vedran Kasalica
 */
public class DBMappingImpl implements DBMapping {

	/** Table that is being mapped */
	private final String table;

	/** Columns from the table */
	private final List<String> columns;

	/** Predicate that the query is being mapped to */
	private final String predicate;

	public DBMappingImpl(String table, List<String> columns, String predicate) {
		super();
		this.table = table;
		this.columns = columns;
		this.predicate = predicate;
	}

	public DBMappingImpl(String table, String cols, String predicate) {
		super();
		this.table = table;
		this.columns = Arrays.asList(cols.split(","));
		this.predicate = predicate;

	}

	@Override
	public List<String> getColumns() {
		if (columns == null)
			return Collections.<String>emptyList();
		return columns;
	}

	@Override
	public String getColumnsString() {
		if (columns == null)
			return "";

		String tmpCols = new String("");
		for (int i = 0; i < columns.size(); i++)
			tmpCols = tmpCols.concat(columns.get(i) + ",");

		if (columns.size() > 0)
			tmpCols = tmpCols.substring(0, tmpCols.length() - 1);

		return tmpCols;
	}

	@Override
	public String getTable() {
		return table;
	}

	@Override
	public String getPredicate() {
		return predicate;
	}

	// TODO
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + table.hashCode();
		result = prime * result + (columns == null ? 0 : columns.hashCode());
		result = prime * result + predicate.hashCode();
		return result;
	}

	@Override
	public String toString() {
		System.out.println("DBMappingImpl.toString() called");
		String tmpVar = new String("");
		String tmpCols = new String("");
		for (int i = 0; i < columns.size(); i++) {
			tmpVar = tmpVar.concat("X" + "" + i + ",");
			tmpCols = tmpCols.concat(columns.get(i) + ",");
		}
		if (columns.size() > 0) {
			tmpVar = tmpVar.substring(0, tmpVar.length() - 1);
			tmpCols = tmpCols.substring(0, tmpCols.length() - 1);
		}
		return predicate + "(" + tmpVar + ") :- odbc_sql([],'SELECT " + tmpCols + " FROM "+table+"', [" + tmpVar
				+ "])";
	}

}
