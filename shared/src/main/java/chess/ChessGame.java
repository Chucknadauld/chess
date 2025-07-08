package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
        this.lastMove = null;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return Collections.emptyList();
        }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            possibleMoves.addAll(getEnPassantMoves(startPosition, piece));
        }
        
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessBoard simulatedBoard = board.copy();

            ChessPiece movingPiece = simulatedBoard.getPiece(startPosition);
            simulatedBoard.addPiece(move.getStartPosition(), null);

            if (move.getPromotionPiece() != null) {
                simulatedBoard.addPiece(move.getEndPosition(),
                        new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
            } else {
                simulatedBoard.addPiece(move.getEndPosition(), movingPiece);
            }

            // En Passant -_0
            boolean isEnPassant = piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    startPosition.getColumn() != move.getEndPosition().getColumn() &&
                    board.getPiece(move.getEndPosition()) == null;

            if (isEnPassant) {
                ChessPosition capturedPawnPos = new ChessPosition(startPosition.getRow(), move.getEndPosition().getColumn());
                simulatedBoard.addPiece(capturedPawnPos, null);
            }

            ChessGame testGame = new ChessGame();
            testGame.setBoard(simulatedBoard);

            if (!testGame.isInCheck(piece.getTeamColor())) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("Invalid piece.");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Wait your turn!");
        }

        Collection<ChessMove> legalMoves = validMoves(start);
        if (!legalMoves.contains(move)) {
            throw new InvalidMoveException("That move is not allowed!");
        }

        boolean isCastling = false;
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int colDiff = start.getColumn() - end.getColumn();
            if (colDiff == 2 || colDiff == -2) {
                isCastling = true;
            }
        }
        boolean isEnPassant = piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                start.getColumn() != end.getColumn() &&
                board.getPiece(end) == null;

        if (move.getPromotionPiece() != null) {
            ChessPiece promoted = new ChessPiece(teamTurn, move.getPromotionPiece());
            promoted.setMoved(true);
            board.addPiece(end, promoted);
        } else {
            board.addPiece(end, piece);
            piece.setMoved(true);
        }

        board.addPiece(start, null);

        if (isCastling) {
            castling(move);
        }

        if (isEnPassant) {
            ChessPosition capturedPawnPos = new ChessPosition(start.getRow(), end.getColumn());
            board.addPiece(capturedPawnPos, null);
        }

        lastMove = move;
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {

                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }

        return isKingInDanger(teamColor, kingPosition, board);
    }

    static boolean isKingInDanger(TeamColor teamColor, ChessPosition kingPosition, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);

                if (isEnemyPiece(piece, teamColor) && threatensKing(piece, position, board, kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEnemyPiece(ChessPiece piece, TeamColor teamColor) {
        return piece != null && piece.getTeamColor() != teamColor;
    }

    private static boolean threatensKing(ChessPiece piece, ChessPosition from, ChessBoard board, ChessPosition kingPosition) {
        for (ChessMove move : piece.pieceMoves(board, from)) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return hasNoMoves(teamColor);
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return hasNoMoves(teamColor);
    }

    private boolean hasNoMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn && Objects.equals(lastMove, chessGame.lastMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn, lastMove);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private Collection<ChessMove> getEnPassantMoves(ChessPosition startPosition, ChessPiece piece) {
        Collection<ChessMove> moves = new ArrayList<>();
        
        if (lastMove == null) {
            return moves;
        }
 
        ChessPiece lastPiece = board.getPiece(lastMove.getEndPosition());
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return moves;
        }

        int rowDiff = lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow();
        if (rowDiff != 2 && rowDiff != -2) {
            return moves;
        }

        if (startPosition.getRow() != lastMove.getEndPosition().getRow()) {
            return moves;
        }
        
        int colDiff = startPosition.getColumn() - lastMove.getEndPosition().getColumn();
        if (colDiff != 1 && colDiff != -1) {
            return moves;
        }

        if (piece.getTeamColor() == lastPiece.getTeamColor()) {
            return moves;
        }

        int direction = 1;
        if (piece.getTeamColor() == TeamColor.BLACK) {
            direction = -1;
        }
        
        int newRow = startPosition.getRow() + direction;
        int newCol = lastMove.getEndPosition().getColumn();
        ChessPosition captureSquare = new ChessPosition(newRow, newCol);

        if (newRow >= 1 && newRow <= 8 && board.getPiece(captureSquare) == null) {
            moves.add(new ChessMove(startPosition, captureSquare, null));
        }
        
        return moves;
    }

    public void castling(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece king = board.getPiece(end);

        if (king == null || king.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }

        int row = start.getRow();
        int startCol = start.getColumn();
        int endCol = end.getColumn();

        if (endCol - startCol == 2) {
            // King side
            ChessPosition rookStart = new ChessPosition(row, 8);
            ChessPosition rookEnd = new ChessPosition(row, 6);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
            if (rook != null) {
                rook.setMoved(true);
            }
        } else if (startCol - endCol == 2) {
            // Queen side
            ChessPosition rookStart = new ChessPosition(row, 1);
            ChessPosition rookEnd = new ChessPosition(row, 4);
            ChessPiece rook = board.getPiece(rookStart);
            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
            if (rook != null) {
                rook.setMoved(true);
            }
        }
    }
}
