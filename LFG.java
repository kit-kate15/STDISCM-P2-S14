import java.util.*;
import java.util.concurrent.*;


class Dungeon {
    private final int id;
    private int partiesServed = 0;
    private int totalTimeServed = 0;
    private boolean isActive = false;
    private String currentParty = "None";

    public Dungeon(int id) {
        this.id = id;
    }

    //Allows a party to enter the dungeon and simulates the time spent inside
    public synchronized void enterDungeon(String party, int time) {
        isActive = true;
        currentParty = party;
        partiesServed++;
        totalTimeServed += time;
        printStatus();

        try {
            Thread.sleep(time * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        isActive = false;
        currentParty = "None";
        printStatus();
    }

    //Checks if the dungeon is currently active
    public synchronized boolean isActive() {
        return isActive;
    }

    //Prints the current status of the dungeon
    public synchronized void printStatus() {
        System.out.println("-----------------------------------------------------------");
        System.out.println("[Dungeon ID] : " + id);
        System.out.println("[Party Served] : " + partiesServed);
        System.out.println("[Total Time Served] : " + totalTimeServed + " seconds");
        System.out.println("[Party Inside the Dungeon] : " + currentParty);
        System.out.println("[Dungeon Status] : " + (isActive ? "Active" : "Empty"));
        System.out.println("-----------------------------------------------------------");
    }

    public synchronized int getPartiesServed() {
        return partiesServed;
    }

    public synchronized int getTotalTimeServed() {
        return totalTimeServed;
    }

    public int getId() {
        return id;
    }
}

public class LFG {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n, t, h, d, t1, t2;

        //Input validation 
        do {
            System.out.print("Enter number of concurrent dungeons (>=1): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            n = scanner.nextInt();
            if (n < 1) {
                System.out.println("Error: Number of concurrent dungeons must be at least 1.");
            }
        } while (n < 1);

        do {
            System.out.print("Enter number of tank players (>=0): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            t = scanner.nextInt();
            if (t < 0) {
                System.out.println("Error: Number of tank players cannot be negative.");
            }
        } while (t < 0);

        do {
            System.out.print("Enter number of healer players (>=0): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            h = scanner.nextInt();
            if (h < 0) {
                System.out.println("Error: Number of healer players cannot be negative.");
            }
        } while (h < 0);

        do {
            System.out.print("Enter number of DPS players (>=0): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            d = scanner.nextInt();
            if (d < 0) {
                System.out.println("Error: Number of DPS players cannot be negative.");
            }
        } while (d < 0);

        do {
            System.out.print("Enter minimum dungeon time (>=1): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            t1 = scanner.nextInt();
            if (t1 < 1) {
                System.out.println("Error: Minimum dungeon time must be at least 1 second.");
            }
        } while (t1 < 1);

        do {
            System.out.print("Enter maximum dungeon time (>= " + t1 + " and <=15): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Error: Invalid input. Please enter an integer.");
                scanner.next();
            }
            t2 = scanner.nextInt();
            if (t2 < t1 || t2 > 15) {
                System.out.println("Error: Maximum dungeon time must be between " + t1 + " and 15 seconds.");
            }
        } while (t2 < t1 || t2 > 15);

        //Dungeon creation and party assignment
        List<Dungeon> dungeons = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < n; i++) {
            dungeons.add(new Dungeon(i + 1));
        }

        ExecutorService executor = Executors.newFixedThreadPool(n);
        Random random = new Random();

        //Processing party entries into dungeons
        while (t >= 1 && h >= 1 && d >= 3) {
            int time = random.nextInt(t2 - t1 + 1) + t1;
            String party = "Tank-" + t + ", Healer-" + h + ", DPS-" + (d - 2) + ", DPS-" + (d - 1) + ", DPS-" + d;
            t -= 1;
            h -= 1;
            d -= 3;
            String finalParty = party;

            synchronized (dungeons) {
                Collections.shuffle(dungeons);
                for (Dungeon dungeon : dungeons) {
                    if (!dungeon.isActive()) {
                        executor.submit(() -> dungeon.enterDungeon(finalParty, time));
                        break;
                    }
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n---------------- Final Dungeon Status ----------------");
        for (Dungeon dungeon : dungeons) {
            dungeon.printStatus();
        }

        System.out.println("\n------------------ Leftover Players ------------------");
        System.out.println("Tank Players Left: " + t);
        System.out.println("Healer Players Left: " + h);
        System.out.println("DPS Players Left: " + d);
        System.out.println("---------------------------------------------------------");

        scanner.close();
    }
}
