public class Lesson4 {
     static volatile int w = 1;
     static final Object mon = new Object();

    public static void main(String[] args) throws InterruptedException {

    Thread thread1 = new Thread(() -> {
        printA();
    });
    Thread thread2 = new Thread(() -> {
        printB();
    });
    Thread thread3 = new Thread(() -> {
        printC();
    });
        thread1.start();
        thread2.start();
        thread3.start();
    }
    public static void printA(){
        synchronized (mon){
            try{
                for (int i = 0; i < 5; i++) {
                   while ( w != 1){
                        mon.wait();
                    }
                    System.out.print("A");
                    w = 2;
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printB(){
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (w != 2) {
                        mon.wait();
                    }
                    System.out.print("B");
                    w = 3;
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printC() {
            synchronized (mon) {
                try {
                    for (int i = 0; i < 5; i++) {
                        while (w != 3) {
                            mon.wait();
                        }
                        System.out.print("C");
                        w = 1;
                        mon.notifyAll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

}
