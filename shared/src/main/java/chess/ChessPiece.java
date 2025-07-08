package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    private boolean hasMoved = false;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public ChessPiece(ChessPiece other) {
        this.pieceColor = other.pieceColor;
        this.type = other.type;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (this.type == PieceType.KING) {
            return getKingMoves(board, myPosition);
        } else if (this.type == PieceType.QUEEN) {
            return getQueenMoves(board, myPosition);
        } else if (this.type == PieceType.BISHOP) {
            return getBishopMoves(board, myPosition);
        } else if (this.type == PieceType.KNIGHT) {
            return getKnightMoves(board, myPosition);
        } else if (this.type == PieceType.ROOK) {
            return getRookMoves(board, myPosition);
        } else {
            return getPawnMoves(board, myPosition);
        }
    }

    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] possibleMoves = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
        };

        checkPossibleMoves(moves, board, position, possibleMoves);
        checkCastlingMoves(moves, board, position);

        return moves;
    }

    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        moves.addAll(getRookMoves(board, position));
        moves.addAll(getBishopMoves(board, position));

        return moves;
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        collectSlidingMoves(moves, board, position, 1, 1);
        collectSlidingMoves(moves, board, position, 1, -1);
        collectSlidingMoves(moves, board, position, -1, 1);
        collectSlidingMoves(moves, board, position, -1, -1);
        return moves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] possibleMoves = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
                {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };

        checkPossibleMoves(moves, board, position, possibleMoves);
        return moves;
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        collectSlidingMoves(moves, board, position, 1, 0);
        collectSlidingMoves(moves, board, position, -1, 0);
        collectSlidingMoves(moves, board, position, 0, 1);
        collectSlidingMoves(moves, board, position, 0, -1);
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int row = position.getRow();
        int col = position.getColumn();

        int direction = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (this.pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // One square forward
        ChessPosition oneForward = new ChessPosition(row + direction, col);
        if (board.getPiece(oneForward) == null) {
            handleMoveOrPromotion(moves, position, oneForward, row + direction == promotionRow);
        }

        // Two squares forward
        if (row == startRow) {
            ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
            if (board.getPiece(twoForward) == null && board.getPiece(oneForward) == null) {
                moves.add(new ChessMove(position, twoForward, null));
            }
        }

        // Diagonal attacks
        handlePawnDiagonalAttack(moves, board, position, row, col - 1, direction, promotionRow);
        handlePawnDiagonalAttack(moves, board, position, row, col + 1, direction, promotionRow);

        return moves;
    }

    // Helper function for sliding pieces aka bishop, knight, and rook
    private void collectSlidingMoves(List<ChessMove> moves, ChessBoard board, ChessPosition from, int rowDelta, int colDelta) {
        int row = from.getRow() + rowDelta;
        int col = from.getColumn() + colDelta;

        while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece target = board.getPiece(newPos);

            if (target == null) {
                moves.add(new ChessMove(from, newPos, null));
            } else {
                if (target.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(from, newPos, null));
                }
                break;
            }

            row += rowDelta;
            col += colDelta;
        }
    }

    // Helper function for pawn promotions
    private void addPromotionMoves(List<ChessMove> moves, ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, PieceType.QUEEN));
        moves.add(new ChessMove(from, to, PieceType.ROOK));
        moves.add(new ChessMove(from, to, PieceType.BISHOP));
        moves.add(new ChessMove(from, to, PieceType.KNIGHT));
    }

    // Helper function for pawn moves
    private void handleMoveOrPromotion(List<ChessMove> moves, ChessPosition from, ChessPosition to, boolean isPromotion) {
        if (isPromotion) {
            addPromotionMoves(moves, from, to);
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

    // Helper function for pawn attacks
    private void handlePawnDiagonalAttack(List<ChessMove> moves, ChessBoard board, ChessPosition from,
                                          int row, int col, int direction, int promotionRow) {
        if (col < 1 || col > 8) {
            return;
        }

        ChessPosition targetPos = new ChessPosition(row + direction, col);
        ChessPiece target = board.getPiece(targetPos);

        if (target != null && target.getTeamColor() != this.pieceColor) {
            boolean isPromotion = row + direction == promotionRow;
            handleMoveOrPromotion(moves, from, targetPos, isPromotion);
        }
    }

    // Helper function for checking possible moves (used in getKnightMoves and getKingMoves)
    private void checkPossibleMoves(List<ChessMove> moves, ChessBoard board, ChessPosition position, int[][] changes) {
        for (int[] change : changes) {
            int newRow = position.getRow() + change[0];
            int newCol = position.getColumn() + change[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(newPosition);

                if (target == null || target.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
    }

    private void checkCastlingMoves(List<ChessMove> moves, ChessBoard board, ChessPosition kingPosition) {
        if (this.hasMoved || this.type != PieceType.KING) return;

        ChessGame.TeamColor color = this.pieceColor;
        int row = kingPosition.getRow();

        // King side castling
        ChessPosition rookPos = new ChessPosition(row, 8);
        ChessPiece rook = board.getPiece(rookPos);

        if (rook != null && rook.getPieceType() == PieceType.ROOK && !rook.hasMoved()) {
            ChessPosition square1 = new ChessPosition(row, 6);
            ChessPosition square2 = new ChessPosition(row, 7);

            if (board.getPiece(square1) == null && board.getPiece(square2) == null &&
                    !isSquareInCheck(board, kingPosition, color) &&
                    !isSquareInCheck(board, square1, color) &&
                    !isSquareInCheck(board, square2, color)) {
                moves.add(new ChessMove(kingPosition, square2, null));
            }
        }

        // Queen side castling
        rookPos = new ChessPosition(row, 1);
        rook = board.getPiece(rookPos);

        if (rook != null && rook.getPieceType() == PieceType.ROOK && !rook.hasMoved()) {
            ChessPosition square1 = new ChessPosition(row, 2);
            ChessPosition square2 = new ChessPosition(row, 3);
            ChessPosition square3 = new ChessPosition(row, 4);

            if (board.getPiece(square1) == null && board.getPiece(square2) == null && board.getPiece(square3) == null &&
                    !isSquareInCheck(board, kingPosition, color) &&
                    !isSquareInCheck(board, square2, color) &&
                    !isSquareInCheck(board, square3, color)) {
                moves.add(new ChessMove(kingPosition, square2, null));
            }
        }
    }

    private boolean isSquareInCheck(ChessBoard board, ChessPosition square, ChessGame.TeamColor color) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition from = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(from);

                if (attacker != null && attacker.getTeamColor() != color) {
                    Collection<ChessMove> theirMoves = attacker.pieceMoves(board, from);
                    for (ChessMove move : theirMoves) {
                        if (move.getEndPosition().equals(square)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
