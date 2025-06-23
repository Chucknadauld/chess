package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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

        for (int[] possibleMove : possibleMoves) {
            int newRow = position.getRow() + possibleMove[0];
            int newCol = position.getColumn() + possibleMove[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

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

        int newColRight = position.getColumn() + 1;
        int newColLeft;
        int newRowUp = position.getRow() + 1;
        int newRowDown;

        // Up + right
        while (newRowUp <= 8 && newColRight <= 8) {
            ChessPosition newPosition = new ChessPosition(newRowUp, newColRight);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowUp++;
            newColRight++;
        }

        // Up + left
        newRowUp = position.getRow() + 1;
        newColLeft = position.getColumn() - 1;
        while (newRowUp <= 8 && newColLeft >= 1) {
            ChessPosition newPosition = new ChessPosition(newRowUp, newColLeft);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowUp++;
            newColLeft--;
        }

        // Down + right
        newRowDown = position.getRow() - 1;
        newColRight = position.getColumn() + 1;
        while (newRowDown >= 1 && newColRight <= 8) {
            ChessPosition newPosition = new ChessPosition(newRowDown, newColRight);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowDown--;
            newColRight++;
        }

        // Down + left
        newRowDown = position.getRow() - 1;
        newColLeft = position.getColumn() - 1;
        while (newRowDown >= 1 && newColLeft >= 1) {
            ChessPosition newPosition = new ChessPosition(newRowDown, newColLeft);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowDown--;
            newColLeft--;
        }

        return moves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] possibleMoves = {
            {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
            {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };

        for (int[] possibleMove : possibleMoves) {
            int newRow = position.getRow() + possibleMove[0];
            int newCol = position.getColumn() + possibleMove[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        // Right
        int newColRight = position.getColumn() + 1;
        while (newColRight <= 8) {
            ChessPosition newPosition = new ChessPosition(position.getRow(), newColRight);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newColRight++;
        }

        // Left
        int newColLeft = position.getColumn() - 1;
        while (newColLeft >= 1) {
            ChessPosition newPosition = new ChessPosition(position.getRow(), newColLeft);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newColLeft--;
        }

        // Up
        int newRowUp = position.getRow() + 1;
        while (newRowUp <= 8) {
            ChessPosition newPosition = new ChessPosition(newRowUp, position.getColumn());
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowUp++;
        }

        // Down
        int newRowDown = position.getRow() - 1;
        while (newRowDown >= 1) {
            ChessPosition newPosition = new ChessPosition(newRowDown, position.getColumn());
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }
            newRowDown--;
        }

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
            if (row + direction == promotionRow) {
                moves.add(new ChessMove(position, oneForward, PieceType.QUEEN));
                moves.add(new ChessMove(position, oneForward, PieceType.ROOK));
                moves.add(new ChessMove(position, oneForward, PieceType.BISHOP));
                moves.add(new ChessMove(position, oneForward, PieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(position, oneForward, null));
            }
        }

        // Two squares forward
        if (row == startRow) {
            ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
            if (board.getPiece(twoForward) == null && board.getPiece(oneForward) == null) {
                moves.add(new ChessMove(position, twoForward, null));
            }
        }

        // Diagonal attacks
        if (col - 1 >= 1) {
            ChessPosition diagonalLeft = new ChessPosition(row + direction, col - 1);
            ChessPiece targetPieceLeft = board.getPiece(diagonalLeft);
            if (targetPieceLeft != null && targetPieceLeft.getTeamColor() != this.pieceColor) {
                if (row + direction == promotionRow) {
                    moves.add(new ChessMove(position, diagonalLeft, PieceType.QUEEN));
                    moves.add(new ChessMove(position, diagonalLeft, PieceType.ROOK));
                    moves.add(new ChessMove(position, diagonalLeft, PieceType.BISHOP));
                    moves.add(new ChessMove(position, diagonalLeft, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(position, diagonalLeft, null));
                }
            }
        }

        if (col + 1 <= 8) {
            ChessPosition diagonalRight = new ChessPosition(row + direction, col + 1);
            ChessPiece targetPieceRight = board.getPiece(diagonalRight);
            if (targetPieceRight != null && targetPieceRight.getTeamColor() != this.pieceColor) {
                if (row + direction == promotionRow) {
                    moves.add(new ChessMove(position, diagonalRight, PieceType.QUEEN));
                    moves.add(new ChessMove(position, diagonalRight, PieceType.ROOK));
                    moves.add(new ChessMove(position, diagonalRight, PieceType.BISHOP));
                    moves.add(new ChessMove(position, diagonalRight, PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(position, diagonalRight, null));
                }
            }
        }

        return moves;
    }
}
