package pagerank;

public class MatrixElement {
	
	private int rowNumber;
	private int columnNumber;
	private int squareMatrixSize;
	
	public MatrixElement(int rowNumber, int columnNumber, int squareMatrixSize) {
		this.rowNumber = rowNumber;
		this.columnNumber = columnNumber;
		this.squareMatrixSize = squareMatrixSize;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MatrixElement)) {
			return false;
		}
		
		if (this.rowNumber == ((MatrixElement) other).getRowNumber() &&
			this.columnNumber == ((MatrixElement) other).getColumnNumber()) {
			return true;
		} else {
			return false;
		}
		
	}
	
	@Override
	public int hashCode() {
		return this.rowNumber * this.squareMatrixSize + this.columnNumber;
	}
}
