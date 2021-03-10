package dk.easv.bll.bot;

import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//virker ikke
public class TwoLayerBot implements IBot {

    private static final String BOTNAME = "Two Layer bot(ik færdig)";
    Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        int thisPlayer = getCurrentPlayer(state);
        int oppPlayer=(thisPlayer +1) %2;
        List<IMove> winMoves = getWinningMoves(state, thisPlayer);
        if(!winMoves.isEmpty()) {
            for(IMove currentMove: winMoves) {
                GameState gs = new GameState(state);
                GameManager gm = new GameManager(gs);
                gm.updateGame(currentMove);
                List<IMove> loseMoves = getWinningMoves(gm.getCurrentState(), oppPlayer);
                if(loseMoves.isEmpty()){
                    return currentMove;
                }

            }
        }
        else{
            List<IMove> oppWinMoves = getWinningMoves(state, oppPlayer);
            System.out.print(oppWinMoves.size());
            if(!oppWinMoves.isEmpty()) {
                return oppWinMoves.get(0);
            }
        }


        return state.getField().getAvailableMoves().get(random.nextInt(state.getField().getAvailableMoves().size()));
    }

    private boolean isWinningMove(String[][] board, IMove move, String player){
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
            if(isWinningMove(state.getField().getBoard(),move,player))
                winningMoves.add(move);
        }
        return winningMoves;
    }

    private int getCurrentPlayer(IGameState state){
        return state.getMoveNumber()%2;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
