import java.util.concurrent.*;
import java.util.*;

class DungeonQueue {
    private final Semaphore instanceSemaphore;
    private final int t1, t2;
    private int totalPartiesServed = 0;
    private int totalTimeServed = 0;
    private final Object lock = new Object();
    private final Map<Integer, Integer> activeDungeons = new ConcurrentHashMap<>();
    private final int maxInstances;
    private final int totalTanks, totalHealers, totalDps;

    public DungeonQueue(int maxInstances, int t1, int t2, int totalTanks, int totalHealers, int totalDps) {
        this.instanceSemaphore = new Semaphore(maxInstances);
        this.t1 = t1;
        this.t2 = t2;
        this.maxInstances = maxInstances;
        this.totalTanks = totalTanks;
        this.totalHealers = totalHealers;
        this.totalDps = totalDps;
    }

    public void runDungeon(int partyId, int dungeonId) {
        try {
            instanceSemaphore.acquire();
            int dungeonTime = new Random().nextInt(t2 - t1 + 1) + t1;
            
            synchronized (lock) {
                activeDungeons.put(dungeonId, dungeonTime);
                printDungeonStatus();
            }
            
            for (int i = 0; i < dungeonTime; i++) {
                Thread.sleep(1000);
                synchronized (lock) {
                    activeDungeons.put(dungeonId, dungeonTime - i - 1);
                    printDungeonStatus();
                }
            }
            
            synchronized (lock) {
                activeDungeons.remove(dungeonId);
                totalPartiesServed++;
                totalTimeServed += dungeonTime;
                printDungeonStatus();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            instanceSemaphore.release();
        }
    }

    private void printDungeonStatus() {
        clearConsole();
        System.out.println("\n------ INPUT SUMMARY ------");
        System.out.println("Max Concurrent Instances: " + maxInstances);
        System.out.println("Tanks in Queue: " + totalTanks);
        System.out.println("Healers in Queue: " + totalHealers);
        System.out.println("DPS in Queue: " + totalDps);
        System.out.println("Min Dungeon Time: " + t1 + "s");
        System.out.println("Max Dungeon Time: " + t2 + "s");
        System.out.println("----------------------------\n");
        
        System.out.println("---------------------------------- Dungeon Status ----------------------------------");
        for (int i = 0; i < maxInstances; i++) {
            if (activeDungeons.containsKey(i)) {
                System.out.println("[DungeonID: " + i + "] - [active]");
                System.out.println("[Party Served: " + totalPartiesServed + "]");
                System.out.println("[Total Time Served: " + totalTimeServed + "s]");
                System.out.println("[Time Remaining: " + activeDungeons.get(i) + "s]");
            } else {
                System.out.println("[DungeonID: " + i + "] - [empty]");
                System.out.println("[Party Served: " + totalPartiesServed + "]");
                System.out.println("[Total Time Served: " + totalTimeServed + "s]");
            }
            System.out.println("------------------------------------------------------------------------------------");
        }
    }

    private void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Error clearing console");
        }
    }
}

class LFGQueue {
    private final Queue<String> tanks = new LinkedList<>();
    private final Queue<String> healers = new LinkedList<>();
    private final Queue<String> dps = new LinkedList<>();
    private int partyCounter = 0;
    private final DungeonQueue dungeonQueue;

    public LFGQueue(DungeonQueue dungeonQueue) {
        this.dungeonQueue = dungeonQueue;
    }

    public void addPlayer(String role, int count) {
        for (int i = 1; i <= count; i++) {
            if (role.equals("tank")) {
                tanks.add("Tank-" + i);
            } else if (role.equals("healer")) {
                healers.add("Healer-" + i);
            } else if (role.equals("dps")) {
                dps.add("DPS-" + i);
            }
        }
    }

    public void formAndRunParties() {
        List<Thread> threads = new ArrayList<>();
        int dungeonId = 0;
        while (!tanks.isEmpty() && !healers.isEmpty() && dps.size() >= 3) {
            String tank = tanks.poll();
            String healer = healers.poll();
            String dps1 = dps.poll();
            String dps2 = dps.poll();
            String dps3 = dps.poll();
            
            partyCounter++;
            System.out.println("Formed Party " + partyCounter + ": [" + tank + ", " + healer + ", " + dps1 + ", " + dps2 + ", " + dps3 + "]");
            
            int finalDungeonId = dungeonId++;
            Thread thread = new Thread(() -> dungeonQueue.runDungeon(partyCounter, finalDungeonId));
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class LFGDungeonQueue {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter max concurrent instances: ");
        int n = scanner.nextInt();
        System.out.print("Enter number of tanks in queue: ");
        int t = scanner.nextInt();
        System.out.print("Enter number of healers in queue: ");
        int h = scanner.nextInt();
        System.out.print("Enter number of DPS in queue: ");
        int d = scanner.nextInt();
        System.out.print("Enter minimum dungeon time: ");
        int t1 = scanner.nextInt();
        System.out.print("Enter maximum dungeon time: ");
        int t2 = scanner.nextInt();
        scanner.close();
        
        DungeonQueue dungeonQueue = new DungeonQueue(n, t1, t2, t, h, d);
        LFGQueue lfgQueue = new LFGQueue(dungeonQueue);
        
        lfgQueue.addPlayer("tank", t);
        lfgQueue.addPlayer("healer", h);
        lfgQueue.addPlayer("dps", d);
        
        lfgQueue.formAndRunParties();
    }
}
