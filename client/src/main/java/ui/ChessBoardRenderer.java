package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.HashSet;

public class ChessBoardRenderer {
    private HashSet<ChessPosition> highlightedSquares;

    public ChessBoardRenderer() {
        this.highlightedSquares = new HashSet<>();
    }

    public void setHighlightedSquares(HashSet<ChessPosition> squares) {
        this.highlightedSquares = squares;
    }

    public void clearHighlights() {
        this.highlightedSquares.clear();
    }

    public void displayBoard(String[][] board, boolean whiteBottom) {
        drawChessBoard(board, whiteBottom);
    }

    public void displayGameBoard(ChessGame game, String playerColor) {
        boolean whiteBottom = playerColor == null || playerColor.equals("WHITE");
        String[][] board = convertGameToBoard(game);
        
        System.out.println();
        drawChessBoard(board, whiteBottom);
        System.out.println();
    }

    private String[][] convertGameToBoard(ChessGame game) {
        String[][] board = new String[8][8];
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chess.ChessPosition pos = new chess.ChessPosition(row + 1, col + 1);
                chess.ChessPiece piece = game.getBoard().getPiece(pos);
                
                if (piece == null) {
                    board[row][col] = EscapeSequences.EMPTY;
                } else {
                    board[row][col] = getPieceSymbol(piece);
                }
            }
        }
        
        return board;
    }

    private void drawChessBoard(String[][] board, boolean whiteBottom) {
        printColumnHeaders(whiteBottom);
        
        for (int i = 0; i < 8; i++) {
            int row = whiteBottom ? 7 - i : i;
            int rowLabel = row + 1;
            printRow(board, row, rowLabel, !whiteBottom);
        }
        
        printColumnHeaders(whiteBottom);
    }

    private void printColumnHeaders(boolean whiteBottom) {
        System.out.print("   ");
        for (int i = 0; i < 8; i++) {
            char col = whiteBottom ? (char) ('a' + i) : (char) ('h' - i);
            System.out.print(" " + col + " ");
        }
        System.out.println("   ");
    }

    private void printRow(String[][] board, int row, int rowLabel, boolean flipped) {
        System.out.print(" " + rowLabel + " ");

        for (int i = 0; i < 8; i++) {
            int col = flipped ? 7 - i : i;
            boolean isLightSquare = (row + col) % 2 != 0;
            ChessPosition pos = new ChessPosition(row + 1, col + 1);
            boolean isHighlighted = highlightedSquares.contains(pos);

            if (isHighlighted) {
                System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            } else if (isLightSquare) {
                System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            } else {
                System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            }

            System.out.print(board[row][col]);
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        }

        System.out.println(" " + rowLabel + " ");
    }

    private String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        
        switch (piece.getPieceType()) {
            case KING:
                return isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN:
                return isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP:
                return isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT:
                return isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK:
                return isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN:
                return isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            default:
                return EscapeSequences.EMPTY;
        }
    }

    public String[][] createEmptyBoard() {
        String[][] board = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = EscapeSequences.EMPTY;
            }
        }
        return board;
    }
}