package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        board[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        return board[row][col];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        clearBoard();
        addPawns(2, ChessGame.TeamColor.WHITE);
        addPawns(7, ChessGame.TeamColor.BLACK);
        addBackRow(1, ChessGame.TeamColor.WHITE);
        addBackRow(8, ChessGame.TeamColor.BLACK);
    }

    private void clearBoard() {
        board = new ChessPiece[8][8];
    }

    private void addPawns(int row, ChessGame.TeamColor color) {
        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(row, col);
            ChessPiece piece = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            addPiece(position, piece);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + Arrays.toString(board) +
                '}';
    }

    private void addBackRow(int row, ChessGame.TeamColor color) {
        ChessPiece.PieceType[] organization = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK,
        };

        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(row, col);
            ChessPiece piece = new ChessPiece(color, organization[col - 1]);
            addPiece(position, piece);
        }
    }
}
