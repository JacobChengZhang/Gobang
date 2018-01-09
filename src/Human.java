//import javafx.event.EventHandler;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.Pane;
//
//public class Human implements Move, Runnable{
//    public static PieceInfo pi = null;
//
//    @Override
//    public void run() {
//
//    }
//
//    @Override
//    public PieceInfo move() {
//        synchronized (pi) {
//            while(pi == null) {
//                try {
//                    pi.wait();
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return pi;
//    }
//
//    Human(PieceInfo pi){
//        this.pi = pi;
//    }
//
//    private int color;
//}
