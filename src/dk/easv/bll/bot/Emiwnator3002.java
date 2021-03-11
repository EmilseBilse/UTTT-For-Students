package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Emiwnator3002 implements IBot{



        private static final String BOTNAME = "Emiwnator3002";
        Random random = new Random();

        @Override
        public IMove doMove(IGameState state) {
            List<IMove> winMoves = getWinningMoves(state);
            List<IMove> loseMoves = getLosingMoves(state);
            if(!winMoves.isEmpty() || !loseMoves.isEmpty()) {
                if(!winMoves.isEmpty()){
                    return winMoves.get(0);
                }else {
                    return loseMoves.get(0);
                }


            }


            return state.getField().getAvailableMoves().get(random.nextInt(state.getField().getAvailableMoves().size()));
        }

        private boolean isWinningMove(IGameState state, IMove move, String player){
            String[][] board = state.getField().getBoard();
            boolean isRowWin = true;
            // Row checking
            int startX = move.getX()-(move.getX()%3);
            int endX = startX + 2;
            for (int x = startX; x <= endX; x++) {
                if(x!=move.getX())
                    if(!board[x][move.getY()].equals(player))
                        isRowWin = false;
            }

            boolean isColumnWin=true;
            // Column checking
            int startY = move.getY()-(move.getY()%3);
            int endY = startY + 2;
            for (int y = startY; y <= endY; y++) {
                if(y!=move.getY())
                    if(!board[move.getX()][y].equals(player))
                        isColumnWin = false;
            }


            boolean isDiagWin = true;

            // Diagonal checking left-top to right-bottom

            for(int i = 0; i<=2; i++) {
                int newX = startX+i;
                int newY = startY+i;
                if(!(move.getX()==newX && move.getY()==newY))
                    if(!board[newX][newY].equals(player))
                        isDiagWin=false;
            }

            boolean isOppositeDiagWin = true;
            // Diagonal checking left-bottom to right-top
            if(!(move.getX()==startX && move.getY()==startY+2))
                if(!board[startX][startY+2].equals(player))
                    isOppositeDiagWin=false;
            if(!(move.getX()==startX+1 && move.getY()==startY+1))
                if(!board[startX+1][startY+1].equals(player))
                    isOppositeDiagWin=false;
            if(!(move.getX()==startX+2 && move.getY()==startY))
                if(!board[startX+2][startY].equals(player))
                    isOppositeDiagWin=false;

            return isColumnWin || isDiagWin || isOppositeDiagWin || isRowWin;
        }

        // Compile a list of all available winning moves
        private List<IMove> getWinningMoves(IGameState state){
            String player = "1";
            if(state.getMoveNumber()%2==0)
                player="0";

            List<IMove> avail = state.getField().getAvailableMoves();

            List<IMove> winningMoves = new ArrayList<>();
            for (IMove move:avail) {
                if(isWinningMove(state,move,player))
                    winningMoves.add(move);
            }
            return winningMoves;
        }

        // Compile a list of all available losing moves
        private List<IMove> getLosingMoves(IGameState state){
            String player = "1";
            if(state.getMoveNumber()%2==1)
                player="0";


            List<IMove> avail = state.getField().getAvailableMoves();

            List<IMove> losingMoves = new ArrayList<>();
            for (IMove move:avail) {
                if(isWinningMove(state,move,player))
                    losingMoves.add(move);
            }
            return losingMoves;
        }

        @Override
        public String getBotName() {
            return BOTNAME;
        }
    }

