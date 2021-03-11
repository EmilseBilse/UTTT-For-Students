package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RyeBot3 implements IBot {

    private static final String BOTNAME = "Rye Bot 3";
    Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        return calcMove(state);
    }

    public IMove calcMove(IGameState state) {
        int thisPlayer = getCurrentPlayer(state);
        int oppPlayer=getOppPlayer(state);
        List<IMove> winMoves = getWinningMoves(state, thisPlayer);


        //if the bot is the starting player it goes for the middle
        if(state.getMoveNumber() == 0) {
            return(new Move(4,4));
        }

        //checks local Wins
        if(!winMoves.isEmpty()) {
/*
            for(IMove currentMove: winMoves) {
                GameState gs = new GameState(state);
                GameManager gm = new GameManager(gs);
                gm.updateGame();
                //If is able to win globally do it
                if(gm.getGameOver().equals(GameManager.GameOverState.Win)) {
                    System.out.println("game winning move");
                    return currentMove;
                }
                //check if a local win results in the opponent being able to win globally next time, if not do it
                List<IMove> oppWinMoves = getWinningMoves(gm.getCurrentState(), oppPlayer);
                for(IMove currentOppMove: oppWinMoves) {
                    GameState gs1 = new GameState(gm.getCurrentState());
                    GameManager gm1 = new GameManager(gs1);
                    gm1.updateGame(currentOppMove);
                     if(!gm1.getGameOver().equals(GameManager.GameOverState.Win)) {
                         System.out.println("win move");
                         return currentMove;
                     }
                }
            }


 */

            return winMoves.get(0);
        }

        //checks local blocking

            List<IMove> oppWinMoves = getWinningMoves(state, oppPlayer);
            if(!oppWinMoves.isEmpty()) {
                /*
                for(IMove currentOppMove: oppWinMoves) {
                    GameState gs = new GameState(state);
                    GameManager gm = new GameManager(gs);
                    gm.updateGame(currentOppMove);
                    //checks if the local block vil result in the opponentWinning, if not returns the move
                    if(gm.getGameOver().equals(GameManager.GameOverState.Win)){
                        System.out.println("block move");
                        return currentOppMove;
                    }
                }

                 */
                return oppWinMoves.get(0);
            }


        List<IMove> prefMoves = getPreferredMoves(state);
        List<IMove> betterPrefMoves = new ArrayList<>();
        if(!prefMoves.isEmpty()){
            for (IMove currentPrefMove: prefMoves){
                if(!is3x3Full(state, currentPrefMove)){
                    betterPrefMoves.add(currentPrefMove);
                }
            }

            return betterPrefMoves.get(random.nextInt(prefMoves.size()));
        }
        return state.getField().getAvailableMoves().get(random.nextInt(state.getField().getAvailableMoves().size()));
    }

    private List<IMove> getPreferredMoves(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> returnMoves = new ArrayList<>();
        int thisPlayer = getCurrentPlayer(state);
        int oppPlayer=(thisPlayer +1) %2;

        List<IMove> outerMiddleMoves = new ArrayList<>();
            for (int j : new int[]{0, 2}) {
                outerMiddleMoves.add(new Move(j,1));
            }
            for (int i : new int[]{0, 2}) {
                outerMiddleMoves.add(new Move(1, i));
            }
        List<IMove> cornerMoves = new ArrayList<>();
            for (int i : new int[]{0, 2, 2}) {
                cornerMoves.add(new Move(i,0));
            }
            cornerMoves.add(new Move(2,2));
        IMove middleMove = new Move(1,1);

        // removes moves that let the opponent pick from the entire board next time (skal nok laves bedre)
        List<IMove> smartMoves = new ArrayList<>();
        for (IMove move: availableMoves) {
            GameState gs = new GameState(state);
            GameManager gm = new GameManager(gs);
            gm.updateGame(move);
            if(gm.getCurrentState().getField().getAvailableMoves().size()<9){
                smartMoves.add(move);
            }
        }
        if(smartMoves.isEmpty()){
            smartMoves = availableMoves;
        }

        //adds OuterMiddleMoves
        for(IMove move:smartMoves) {
            for(IMove outerMiddleMove: outerMiddleMoves) {
                if(imovesMatching(move,outerMiddleMove)&&!returnMoves.contains(move)){
                    GameState gs = new GameState(state);
                    GameManager gm = new GameManager(gs);
                    gm.updateGame(move);
                    if(getWinningMoves(gm.getCurrentState(), oppPlayer).isEmpty()){
                        returnMoves.add(move);
                    }
                }
            }
        }
        //adds CornerMoves
        if(returnMoves.isEmpty()) {
            for(IMove move:smartMoves) {
                for(IMove cornerMove: cornerMoves) {
                    if(imovesMatching(move,cornerMove)&&!returnMoves.contains(move)){
                        GameState gs = new GameState(state);
                        GameManager gm = new GameManager(gs);
                        gm.updateGame(move);
                        if(getWinningMoves(gm.getCurrentState(), oppPlayer).isEmpty()){
                            returnMoves.add(move);
                        }
                    }
                }
            }
        }
        //add middleMove
        if(returnMoves.isEmpty()) {
            for(IMove move: smartMoves) {
                if(imovesMatching(move,middleMove) && !returnMoves.contains(move)) {
                    GameState gs = new GameState(state);
                    GameManager gm = new GameManager(gs);
                    gm.updateGame(move);
                    if (getWinningMoves(gm.getCurrentState(), oppPlayer).isEmpty()) {
                        returnMoves.add(move);
                    }
                }
            }
        }

        if(returnMoves.isEmpty()) {
            returnMoves = smartMoves;
        }
        return returnMoves;

    }

    private boolean imovesMatching (IMove move1, IMove move2){
        if(move1.getX() == move2.getX() && move2.getX()== move2.getX()) {
            return true;
        }
        return false;
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
    private List<IMove> getWinningMoves(IGameState state, int chosenPlayer){
        String player = String.valueOf(chosenPlayer);

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move:avail) {
            if(isWinningMove(state,move,player))
                winningMoves.add(move);
        }
        return winningMoves;
    }

    private int getCurrentPlayer(IGameState state){
        int player = 1;
        if(state.getMoveNumber()%2==0)
            player=0;

        return player;
    }

    private int getOppPlayer(IGameState state){
        int player = 1;
        if(state.getMoveNumber()%2==1)
            player = 0;

        return player;
    }

    public boolean isFull(IGameState state) {
        for (int i = 0; i < state.getField().getBoard().length; i++)
            for (int k = 0; k < state.getField().getBoard()[i].length; k++) {
                if(state.getField().getBoard()[i][k]==state.getField().EMPTY_FIELD)
                    return false;
            }
        return true;
    }

    private  boolean is3x3Full(IGameState state, IMove move){


        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        //check col
        for (int i = startY; i < startY + 3; i++) {
            if (!state.getField().getBoard()[move.getX()][i].equals(getCurrentPlayer(state)) || !state.getField().getBoard()[move.getX()][i].equals(getOppPlayer(state)))
                break;
            if (i == startY + 3 - 1) return true;
        }

        //check row
        for (int i = startX; i < startX + 3; i++) {
            if (!state.getField().getBoard()[i][move.getY()].equals(getCurrentPlayer(state)) || !state.getField().getBoard()[i][move.getY()].equals(getOppPlayer(state)))
                break;
            if (i == startX + 3 - 1) return true;
        }

        //check diagonal
        if (localX == localY) {
            //we're on a diagonal
            int y = startY;
            for (int i = startX; i < startX + 3; i++) {
                if (!state.getField().getBoard()[i][y++].equals(getCurrentPlayer(state)) || !state.getField().getBoard()[i][y++].equals(getOppPlayer(state)))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }

        //check anti diagonal
        if (localX + localY == 3 - 1) {
            int less = 0;
            for (int i = startX; i < startX + 3; i++) {
                if (!state.getField().getBoard()[i][(startY + 2)-less++].equals(getCurrentPlayer(state)) || !state.getField().getBoard()[i][(startY + 2)-less++].equals(getOppPlayer(state)))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }

        for (int i = startX; i < startX+3; i++) {
            for (int k = startY; k < startY+3; k++) {
                if(state.getField().getBoard()[i][k].equals(IField.AVAILABLE_FIELD) ||
                        state.getField().getBoard()[i][k].equals(IField.EMPTY_FIELD) )
                    return false;
            }
        }
        return false;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
