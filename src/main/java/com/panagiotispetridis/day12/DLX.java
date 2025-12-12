package com.panagiotispetridis.day12;

import java.util.Arrays;

// AI GENERATED - I can't write DLX :(
/**
 * Array-based DLX specialized for:
 *  - primary columns = grid cells
 *  - secondary constraints = shape counts (handled via used/required arrays)
 *
 * Columns: 0..(nCols-1) = primary grid columns
 * Node indices:
 *   0..(nCols-1) = column header nodes
 *   headerIndex = nCols
 *   nCols+1.. = data nodes
 */
public class DLX {

    private final int nCols;
    private final int nShapes;
    private final int header;   // index of header node
    private int nodeCount;      // next free node index

    // Dancing Links structure
    private final int[] L;
    private final int[] R;
    private final int[] U;
    private final int[] D;
    private final int[] C;      // C[i] = column index for node i
    private final int[] rowOf;  // rowOf[i] = row id for node i
    private final int[] S;      // S[c] = size of column c

    // Shape usage constraints
    private final int[] requiredShape;
    private final int[] usedShape;
    private final int[] rowShape;   // rowShape[rowId] = which shape this row places

    private int nextRowId = 0;
    private boolean solutionFound = false;

    /**
     * @param numGridCols number of grid-cell columns (primary columns)
     * @param numShapes   number of shapes
     * @param maxRows     upper bound on number of rows (placements)
     * @param maxRowLen   upper bound on number of columns per row (cells per shape)
     * @param requiredShape required count per shape (length = numShapes)
     */
    public DLX(int numGridCols, int numShapes, int maxRows, int maxRowLen, int[] requiredShape) {
        this.nCols = numGridCols;
        this.nShapes = numShapes;
        this.requiredShape = requiredShape.clone();
        this.usedShape = new int[numShapes];
        this.rowShape = new int[maxRows];
        Arrays.fill(this.rowShape, -1);

        // Total node capacity:
        //  - nCols column headers
        //  - 1 header node
        //  - maxRows * maxRowLen data nodes
        int totalNodes = nCols + 1 + maxRows * maxRowLen;

        L = new int[totalNodes];
        R = new int[totalNodes];
        U = new int[totalNodes];
        D = new int[totalNodes];
        C = new int[totalNodes];
        rowOf = new int[totalNodes];
        S = new int[nCols + 1];   // include header index slot for safety

        header = nCols;
        nodeCount = nCols + 1;    // column headers + header allocated

        // Initialize column headers as a horizontal ring: header <-> 0..nCols-1
        if (nCols > 0) {
            // horizontal links
            L[0] = header;
            for (int c = 1; c < nCols; c++) {
                L[c] = c - 1;
                R[c - 1] = c;
            }
            R[nCols - 1] = header;
            L[header] = nCols - 1;
            R[header] = 0;
        } else {
            // no columns: header points to itself
            L[header] = header;
            R[header] = header;
        }

        // column headers are self-linked vertically
        for (int c = 0; c < nCols; c++) {
            U[c] = c;
            D[c] = c;
            C[c] = c;
            rowOf[c] = -1;
            S[c] = 0;
        }

        // header is its own vertical ring
        U[header] = header;
        D[header] = header;
        C[header] = header;
        rowOf[header] = -1;
    }

    // Add a row: grid column indices only; shapeId identifies which shape this row places
    public void addRow(int[] cols, int shapeId) {
        int rowId = nextRowId++;
        rowShape[rowId] = shapeId;

        int first = -1;
        int prevNode = -1;

        for (int col : cols) {
            int node = nodeCount++;
            C[node] = col;
            rowOf[node] = rowId;

            // vertical insert into column col (just above the column header)
            D[node] = col;
            U[node] = U[col];
            D[U[col]] = node;
            U[col] = node;
            S[col]++;

            // horizontal ring for this row
            if (first == -1) {
                first = node;
                L[node] = node;
                R[node] = node;
            } else {
                R[node] = first;
                L[node] = prevNode;
                R[prevNode] = node;
                L[first] = node;
            }

            prevNode = node;
        }
    }

    public boolean solveExists() {
        search();
        return solutionFound;
    }

    private void search() {
        if (solutionFound) return;

        // If there are no columns left, check shape counts
        if (R[header] == header) {
            for (int s = 0; s < nShapes; s++) {
                if (usedShape[s] != requiredShape[s]) {
                    return;
                }
            }
            solutionFound = true;
            return;
        }

        int col = chooseColumn();
        if (col == -1 || S[col] == 0) return;

        cover(col);

        // iterate each row that has a 1 in column col
        for (int r = D[col]; r != col; r = D[r]) {

            int rowId = rowOf[r];
            int shapeId = (rowId >= 0 ? rowShape[rowId] : -1);

            if (shapeId >= 0) {
                usedShape[shapeId]++;
                if (usedShape[shapeId] > requiredShape[shapeId]) {
                    // prune this branch
                    usedShape[shapeId]--;
                    continue;
                }
            }

            // cover all other columns in this row
            for (int j = R[r]; j != r; j = R[j]) {
                cover(C[j]);
            }

            search();
            if (solutionFound) return;

            // rollback
            for (int j = L[r]; j != r; j = L[j]) {
                uncover(C[j]);
            }

            if (shapeId >= 0) {
                usedShape[shapeId]--;
            }
        }

        uncover(col);
    }

    // Choose the primary column with smallest size
    private int chooseColumn() {
        int bestCol = -1;
        int bestSize = Integer.MAX_VALUE;

        for (int c = R[header]; c != header; c = R[c]) {
            int sz = S[c];
            if (sz < bestSize) {
                bestSize = sz;
                bestCol = c;
                if (bestSize == 1) break; // perfect heuristic
            }
        }
        return bestCol;
    }

    private void cover(int c) {
        // remove column header from horizontal list
        R[L[c]] = R[c];
        L[R[c]] = L[c];

        // for each row in this column
        for (int i = D[c]; i != c; i = D[i]) {
            // unlink row i from other columns
            for (int j = R[i]; j != i; j = R[j]) {
                int col = C[j];
                U[D[j]] = U[j];
                D[U[j]] = D[j];
                S[col]--;
            }
        }
    }

    private void uncover(int c) {
        // reverse of cover
        for (int i = U[c]; i != c; i = U[i]) {
            for (int j = L[i]; j != i; j = L[j]) {
                int col = C[j];
                S[col]++;
                U[D[j]] = j;
                D[U[j]] = j;
            }
        }

        R[L[c]] = c;
        L[R[c]] = c;
    }
}