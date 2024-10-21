package org.multiprogramming_os;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Phase2 {
    static char memory[][] = new char[300][4];
    static char instructionRegister[] = new char[4];
    static char aRegister[] = new char[4];

    static int instructionCounter;
    static int SI;
    static int M = 0;

    // toggle register C
    static boolean toggleRegister = false;

    // a user defined structure for Process/Program control block
    static class PCB {
        // job id, total time limit, total line limit
        static int TTL = 0;
        static int JID = 0;
        static int TLL = 0;
    }

    // counter for faults
    static int TTC = 0;
    static int LLC = 0;

    // interrupts
    static int TI = 0;
    static int PI = 0;

    // has base address of page table
    static int PTR;

    // some helper data structures to keep track of random number generarted by
    // Allocate() function and also its index to keep track of that
    static int[] rndmGenerated = new int[100];
    static int rndmGeneratedIndex;

    // some more helper data structures
    // a data structure to keep track of Page Table and an index to keep track of
    // that
    static int currKeyIndex;
    static int[] key = new int[30];
    static int[] value = new int[30];

    // the generated address by the allocate() function
    static int genAddress;

    // for the error msg
    static String ErrMsg = new String();
    static int msgCode;

    // the files to be read
    static File outputFile = new File("output.txt");
    static File inputFile = new File("input_phase2.txt");

    public static BufferedReader input; // Input reader
    public static BufferedWriter output; // Output writer

    // other helper data structures
    // keeps a track on which page we are in the memory
    static int pageCount = 0;
    static int Program_card_counter;
    static int currentLine;

    // to keep a track whether a job is has written anything in the file or not
    static boolean isWritten = false;
    static boolean terminate;

    // Initialize memory and registers
    public static void init() {
        // clear the memory
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 4; j++) {
                memory[i][j] = ' ';
            }
        }

        // clear the register and the instruction register
        for (int i = 0; i < 4; i++) {
            instructionRegister[i] = ' ';
            aRegister[i] = ' ';
        }

        // reset the counter and interrupts
        instructionCounter = 0;
        toggleRegister = false;

        // Reinitialized Counters for Time Limit and Line Limit
        TTC = 0;
        LLC = 0;

        // Interruption codes initializing, systeminterrupts, program interrupt and time
        // interrupt
        SI = 0;
        PI = 0;
        TI = 0;

        // initialise the helper variables
        pageCount = 0;
        Program_card_counter = 0;
        rndmGeneratedIndex = -1;
        currKeyIndex = -1;
        terminate = false;
    }

    // random number generation between 0-29
    public static int Allocate() {
        Random rand = new Random();
        int address;
        boolean alreadyGenerated = false;

        while (true) {
            address = rand.nextInt(30); // Generate random number between 0 and 29
            for (int i = 0; i <= rndmGeneratedIndex; i++) {
                // check all addresses whether the current generated no is already generated or
                // not
                if (rndmGenerated[i] == address) {
                    // if already generated, set alreadyGenerated to True
                    alreadyGenerated = true;
                    break;
                }
            }

            // if already not generated, break out of the loop, else continue the loop to
            // generate a new number
            if (!alreadyGenerated) {
                break;
            } else {
                // set alreadyGenerated to false, for next iteration
                alreadyGenerated = false;
            }
        }

        rndmGeneratedIndex++;
        rndmGenerated[rndmGeneratedIndex] = address;

        return address;
    }

    // Print the current memory contents
    private static void printMemory() {

        System.out.println("Memory content:");

        // now the size of the memory has become 300 words
        for (int i = 0; i < 300; i++) {
            System.out.print("M[" + i + "]: ");
            for (int j = 0; j < 4; j++) {
                System.out.print(memory[i][j]);
            }
            System.out.println();
        }
    }

    // gets the actual address of the instruction from the page table in the memory
    private static int getRealAddress(int instructionCounter) {
        // check if instruction counter is a multiple of 10 if yes a new page has
        // started
        if (instructionCounter % 10 == 0 && instructionCounter != 0) {
            pageCount++;
        }

        // check for out of bounds access
        if (PTR + pageCount >= memory.length || PTR + pageCount < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid page table access");
        }

        // get the frame number of the instruction from the memory
        int frame = (memory[PTR + pageCount][2] - '0') * 10 + (memory[PTR + pageCount][3] - '0');

        // address is (frame * 10) + offset
        int address = frame * 10 + instructionCounter % 10;

        // check for out of bounds access
        if (address >= memory.length || address < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid memory address");
        }

        // return the real address of the instruction
        return address;
    }

    // convert logical address into physical address
    // it means whenever there is an nstruction GD20, 20 is operand which is the
    // logical address, we have to convert that into physical address
    private static int AddressMap(int logicalAddress) throws IOException {

        // Handle halt instruction separately
        if (instructionRegister[0] == 'H' && instructionRegister[1] == ' ') {
            return -1;
        }

        if (instructionRegister[0] == 'B' && instructionRegister[1] == 'T') {
            return -1;
        }

        // check whether operand is number or not, if not raise the program interrupt,
        // opcode error
        // Validate operand digits
        if (!Character.isDigit(instructionRegister[2]) || !Character.isDigit(instructionRegister[3])) {
            PI = 2;
            MOS();
            return -1;
        }

        // get the frame number for the logical address by diving the logical address by
        // frame size / page size which is 10
        int frameNo = logicalAddress / 10;

        // Check if frame exists in page table
        for (int i = 0; i <= currKeyIndex; i++) {
            if (key[i] == frameNo * 10) {
                return (value[i] * 10) + (logicalAddress % 10);
            }
        }

        // Handle GD and SR instructions for valid page faults
        if ((instructionRegister[0] == 'G' && instructionRegister[1] == 'D') ||
                (instructionRegister[0] == 'S' && instructionRegister[1] == 'R')) {

            if (currKeyIndex >= key.length - 1) {
                System.out.println("Page table full");
                terminate(6);
                return -1;
            }

            // Allocate new frame
            currKeyIndex++;
            key[currKeyIndex] = frameNo * 10;
            value[currKeyIndex] = Allocate();

            // Update page table
            int realAddress = value[currKeyIndex];
            memory[PTR + Program_card_counter][0] = '1';
            memory[PTR + Program_card_counter][2] = (char) (realAddress / 10 + '0');
            memory[PTR + Program_card_counter][3] = (char) (realAddress % 10 + '0');
            Program_card_counter++;

            PI = 0;
            return realAddress * 10 + (logicalAddress % 10);
        }

        // INvalid Page fault for other instructions than GD and SR
        PI = 3;
        MOS();
        return -1;
    }

    // Modified read method to prevent recursion
    private static void read() throws IOException {
        // check if theres a prg interrupt already
        if (PI != 0)
            return; // Don't proceed if there's a pending interrupt

        // get the operand and the frameNo
        int operand = (instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0');
        int frameNo = operand / 10;

        // page table is a key value aarray pair, which is indexed by currKeyIndex
        // Check if frame exists in page table
        boolean frameExists = false;
        for (int i = 0; i <= currKeyIndex; i++) {
            if (key[i] == frameNo * 10) {
                frameExists = true;
                break;
            }
        }

        // If frame doesn't exist, allocate it
        // if (!frameExists) {
        // PI = 3; // Set page fault
        // MOS();
        // return;
        // }

        // if the frame doesnt exist allocate it in the memory and the base address to page table
        if (!frameExists) {
            currKeyIndex++;
            key[currKeyIndex] = frameNo * 10;
            value[currKeyIndex] = Allocate();

            memory[PTR + Program_card_counter][0] = '1';
            memory[PTR + Program_card_counter][2] = (char) (value[currKeyIndex] / 10 +
                    '0');
            memory[PTR + Program_card_counter][3] = (char) (value[currKeyIndex] % 10 +
                    '0');
            Program_card_counter++;
        }

        // Get real address of the instruction
        int row = -1;
        for (int i = 0; i <= currKeyIndex; i++) {
            if (key[i] == frameNo * 10) {
                row = value[i] * 10 + (operand % 10);
                break;
            }
        }

        if (row == -1)
            return; // Should not happen after above checks

        int col = 0;
        String buffer = input.readLine();
        currentLine += (buffer != null) ? buffer.length() + 1 : 0;

        if (buffer == null || buffer.startsWith("$END")) {
            terminate(1);
            return;
        }

        // Skip control cards
        if (buffer.startsWith("$")) {
            return;
        }

        // Write to memory
        for (int i = 0; i < buffer.length() && i < 40; i++) {
            memory[row][col] = buffer.charAt(i);
            col++;
            if (col == 4) {
                row++;
                col = 0;
            }
        }

        SI = 0;
    }

    // In your write() method, modify how you write to the file:
    // Modify the write() method
    private static void write() throws IOException {
        // since there was a PD instruction, increment the line limit counter
        LLC++;
        if (LLC > PCB.TLL) {
            terminate(2);
            return;
        }

        try {
            // is Written checks whether the current job has a first line written to output.txt or not
            if (!isWritten) {
                isWritten = true;
            } else {
                output.write("\n");
            }

            // get address
            int operand = (instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0');
            int row = AddressMap(operand);

            if (row < 0 || row >= memory.length) {
                System.out.println("Invalid memory access in write(): " + row);
                terminate(6);
                return;
            }

            // Create a string builder for the line
            StringBuilder line = new StringBuilder();

            // Add memory contents
            for (int i = row; i < row + 10 && i < memory.length; i++) {
                for (int j = 0; j < 4; j++) {
                    // Only append printable characters or space
                    char c = memory[i][j];
                    if (Character.isLetterOrDigit(c) || c == ' ' || c == '*') {
                        line.append(c);
                    } else {
                        line.append(' ');
                    }
                }
            }

            // Pad with spaces to maintain formatting
            while (line.length() < 40) {
                line.append(' ');
            }

            // write the contents to the output file and flush it to avoid garbage value
            output.write(line.toString());
            output.flush();
        } catch (IOException e) {
            System.out.println("Error writing to output: " + e.getMessage());
            throw e;
        }

        SI = 0;
    }

    // the terminate function
    static void terminate(int msgCode) {
        // makes the terminate flag as true,since there was an interrupt or H command to stop the currnet job
        terminate = true;
        try {
            // a string to store the error message
            StringBuilder errMsg = new StringBuilder();
            errMsg.append("\n");
            errMsg.append(String.format("JOB ID   : %4d", PCB.JID));
            errMsg.append("\n");

            // check the msgCode for the error that has occcured
            switch (msgCode) {
                case 0:
                    errMsg.append(" NO ERROR");
                    break;
                case 1:
                    errMsg.append(" OUT OF DATA");
                    break;
                case 2:
                    errMsg.append(" LINE LIMIT EXCEEDED");
                    break;
                case 3:
                    errMsg.append(" TIME LIMIT EXCEEDED");
                    break;
                case 4:
                    errMsg.append(" OPERATION CODE ERROR");
                    break;
                case 5:
                    errMsg.append(" OPERAND ERROR");
                    break;
                case 6:
                    errMsg.append(" INVALID PAGE FAULT");
                    break;
                case 7:
                    errMsg.append(" TIME LIMIT EXCEEDED & OPERATION CODE ERROR");
                    break;
                case 8:
                    errMsg.append(" TIME LIMIT EXCEEDED & OPERAND ERROR");
                    break;
            }

            // construct the error msg and write to the file
            errMsg.append("\n");
            errMsg.append(String.format("IC       : %d", instructionCounter));
            errMsg.append("\n");
            errMsg.append("IR       : ");
            for (int i = 0; i < 4; i++) {
                errMsg.append(instructionRegister[i]);
            }
            errMsg.append("\n");
            errMsg.append(String.format("TTC      : %d", TTC));
            errMsg.append("\n");
            errMsg.append(String.format("LLC      : %d", LLC));
            errMsg.append("\n\n");

            output.write(errMsg.toString());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // execute user program function, important ahe lmao
    static void executeUserProgram() throws IOException {
        // make a check whether terminate is false, then only continue
        while (!terminate) {
            // Check Time Limit before executing instruction
            if (TTC >= PCB.TTL) {
                TI = 2;
                MOS();
                break;
            }

            // Get real address
            int address;
            try {
                address = getRealAddress(instructionCounter);
            } catch (ArrayIndexOutOfBoundsException e) {
                PI = 3; // Invalid Page Fault
                MOS();
                continue;
            }
		//AtharvaKulkarniIT - Github
            // Load instruction register
            for (int i = 0; i < 4; i++) {
                instructionRegister[i] = memory[address][i];
            }

            // Validate operation code, check for opcode error
            String opcode = "" + instructionRegister[0] + instructionRegister[1];
            if (!(opcode.equals("GD") || opcode.equals("PD") || opcode.equals("H ") ||
                    opcode.equals("LR") || opcode.equals("SR") || opcode.equals("CR") ||
                    opcode.equals("BT"))) {
                PI = 1;
                MOS();
                continue;
            }

            // Validate operand for numeric instructions and operand error
            if (opcode.equals("SR") || opcode.equals("LR") || opcode.equals("CR") || opcode.equals("BT")) {
                if (!Character.isDigit(instructionRegister[2]) ||
                        !Character.isDigit(instructionRegister[3])) {
                    PI = 2;
                    MOS();
                    continue;
                }
            }

            // Increment IC before execution
            instructionCounter++;

            // Increment TTC after validation but before execution, since there was an instruction 
            TTC++;

            // examine which instruction was it and execute
            switch (opcode) {
                case "LR":
                    loadRegister();
                    break;
                case "SR":
                    storeRegister();
                    break;
                case "CR":
                    compareRegister();
                    break;
                case "BT":
                    branchOnTrue();
                    break;
                case "GD":
                    SI = 1;
                    MOS();
                    break;
                case "PD":
                    SI = 2;
                    MOS();
                    break;
                case "H ":
                    SI = 3;
                    MOS();
                    break;
            }
        }
    }


    static void MOS() throws IOException {

        // case1: there is no time interrupt and program interrupt
        if (TI == 0 && PI == 0) {
            switch (SI) {
                case 1: // Read
                    read();
                    break;
                case 2: // Write
                    if (LLC >= PCB.TLL) {
                        terminate(2);
                        return;
                    }
                    write();
                    break;
                case 3: // Terminate
                    terminate(0);
                    break;
            }
        }

        // time interrupt with other interrupts
        else if (TI == 2) {
            if (PI == 1) {
                terminate(7);
            } else if (PI == 2) {
                terminate(8);
            } else if (SI == 2) {
                write();
                terminate(3);
            } else {
                terminate(3);
            }
        }

        // program interrupt
        else if (PI != 0) {
            switch (PI) {
                case 1:
                    terminate(4);
                    break;
                case 2:
                    terminate(5);
                    break;
                case 3:
                    if (SI == 1) {
                        // Allocate frame and update page table
                        int frameNo = (instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0');
                        frameNo /= 10;
                        currKeyIndex++;
                        key[currKeyIndex] = frameNo * 10;
                        value[currKeyIndex] = Allocate();

                        memory[PTR + Program_card_counter][0] = '1';
                        memory[PTR + Program_card_counter][2] = (char) (value[currKeyIndex] / 10 + '0');
                        memory[PTR + Program_card_counter][3] = (char) (value[currKeyIndex] % 10 + '0');
                        Program_card_counter++;

                        PI = 0;
                        instructionCounter--; // Retry the instruction
                    } else {
                        terminate(6);
                    }
                    break;
            }
        }
    }

    // the simulation function, which is called in the executeUserProgram function
    private static void simulation() {

        // increase the total time counter, as instruction is being executed and check
        // if TTC is more than total time limit,if yes then make a time interrupt
        TTC++;

        if (TTC > PCB.TTL) {

            // set the timer interrupt
            TI = 2;

        }

    }

    // the change from the phase 1 to phase 2 , is first you have to map the real
    // address in the instruction functions like LR, SR, CR, BT
    // Load data from memory into the accumulator register R
    // Modify the loadRegister method to handle invalid addresses:
    private static void loadRegister() throws IOException {
        int address = AddressMap((instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0'));

        if (address == -1) {
            return; // Return if address mapping failed
        }

        if (address >= 300) {
            System.out.println("Address out of bounds during load.");
            terminate(6);
            return;
        }

        for (int i = 0; i < 4; i++) {
            aRegister[i] = memory[address][i];
        }
        System.out.println("Loaded into R from memory[" + address + "]: " + String.valueOf(aRegister));
    }

    // Store data from accumulator register R into memory
    private static void storeRegister() throws IOException {

        // get the real address to work with
        int address = AddressMap((instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0'));

        if (address >= 300) {
            System.out.println("Address out of bounds during store.");
            return;
        }
        for (int i = 0; i < 4; i++) {
            memory[address][i] = aRegister[i]; // Store R data into memory
        }
        System.out.println("Stored R into memory[" + address + "]: " + String.valueOf(aRegister));
    }

    // Compare contents of register R with memory
    private static void compareRegister() throws IOException {

        // get the real address to work with
        int address = AddressMap((instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0'));

        if (address >= 300) {
            System.out.println("Address out of bounds during compare.");
            return;
        }
        toggleRegister = true; // Assume equality
        for (int i = 0; i < 4; i++) {
            if (aRegister[i] != memory[address][i]) {
                toggleRegister = false; // Set false if any byte mismatches
                break;
            }
        }
        System.out.println("Compared R with memory[" + address + "], toggleRegister = " + toggleRegister);
    }

    // Branch to memory address if toggle register C is true
    // jump to the frame number mentioned in the operand
    private static void branchOnTrue() {
        if (toggleRegister) {
            int address = (instructionRegister[2] - '0') * 10 + (instructionRegister[3] - '0');
            if (address >= 300) {
                System.out.println("Address out of bounds during branch.");
                return;
            }
            instructionCounter = address; // Set instruction counter to branch address
            System.out.println("Branching to address: " + address);
        } else {
            System.out.println("Branch not taken.");
        }
    }

    // Start Execution function
    static void startExecution() throws IOException {
        // set the instruction counter to 0
        instructionCounter = 0;

        // calling executeUserProgram
        executeUserProgram();
    }

    // the load function
    private static void load() {
        try {

            // take the current line in the buffer
            String buffer;
            while ((buffer = input.readLine()) != null) {
                // In case the first four letters are $AMJ
                if (buffer.startsWith("$AMJ")) {
                    // allocate PTR to a random number
                    PTR = Allocate() * 10;

                    // new job found now initialise the components
                    init();

                    // create and intitialise the PCB
                    PCB.JID = Integer.parseInt(buffer.substring(4, 8));
                    PCB.TTL = Integer.parseInt(buffer.substring(8, 12));
                    PCB.TLL = Integer.parseInt(buffer.substring(12));

                    // initialise the page table
                    for (int i = PTR; i < PTR + 10; i++) {
                        memory[i][0] = '0';
                        memory[i][1] = '*';
                        memory[i][2] = '*';
                        memory[i][3] = '*';
                    }

                    System.out.println("AMJ found!");
                    System.out.println("Job Id : " + PCB.JID);
                    System.out.println("Time Limit : " + PCB.TTL);
                    System.out.println("Line Limit : " + PCB.TLL);

                    // Read next line (program cards)
                    buffer = input.readLine();

                    // Process program cards until $DTA
                    while (buffer != null && !buffer.startsWith("$DTA")) {
                        // take a temp variable to store program's base address
                        int prgCardFrame = Allocate();
                        int prgCardAddress = prgCardFrame * 10;

                        // Update page table
                        memory[PTR + Program_card_counter][0] = '1';
                        memory[PTR + Program_card_counter][2] = (char) (prgCardFrame / 10 + '0');
                        memory[PTR + Program_card_counter][3] = (char) (prgCardFrame % 10 + '0');

                        Program_card_counter++;

                        // Load instructions into memory
                        int col = 0;
                        for (int i = 0; i < buffer.length(); i++) {
                            if (buffer.charAt(i) == ' ') {
                                continue;
                            }

                            memory[prgCardAddress][col] = buffer.charAt(i);
                            col++;

                            if (col == 4) {
                                prgCardAddress++;
                                col = 0;
                            }
                        }

                        buffer = input.readLine();
                    }

                    // Start execution when $DTA is found
                    if (buffer != null && buffer.startsWith("$DTA")) {
                        startExecution();
                    }

                    // Skip data section until $END
                    while (buffer != null && !buffer.startsWith("$END")) {
                        buffer = input.readLine();
                        // keep a track on which line number we are on
                        currentLine += (buffer != null) ? buffer.length() + 1 : 0;
                    }

                    if (buffer != null && buffer.startsWith("$END")) {
                        printMemory();
                        System.out.println(buffer);
                        System.out.println("_____________________END OF JOB_____________________");

                        // Reset isWritten flag for next job
                        isWritten = false;
                    }
                }
            }

        } catch (Exception exception) {
            System.out.println("load(): cannot read the input file:");
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            input = new BufferedReader(new FileReader("C:\\Users\\Hp\\IdeaProjects\\Multiprogramming_OS\\InputForPhase1.txt"));
            output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("output.txt"), StandardCharsets.UTF_8));
            load(); // Load the jobs from input
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
