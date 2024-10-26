# Multiprogramming Operating System

Welcome to the **Multiprogramming Operating System** project! 🎉 This project simulates a multiprogramming environment, implementing various phases of execution and management. Below, you'll find an overview of the two phases and their functionalities.

## Phases Overview

### Phase 1: Instruction Processing 🧩

**Description**: In this phase, the system processes instructions and handles basic memory operations. It reads instructions from an input file, executes them and writes results to an output file.

**Key Features**:
- **Instruction Register (IR)**: Stores the current instruction being executed.
- **Memory Management (M)**: A simulated memory array used for storing and manipulating data.
- **File Operations**: Manages reading from an input file and writing results to an output file.

### Phase 2: Memory and Job Management 🗃️

**Description**: This phase enhances the system by integrating advanced memory management and job handling techniques. It includes dynamic memory allocation, error handling and job execution management to simulate a realistic operating system environment.

**Key Features**:
- **Dynamic Memory Allocation**: Allocates memory dynamically based on job requirements, simulating page faults and memory allocation strategies using a random allocation method to ensure unique page assignments.
- **Error Handling**: Detects and manages various errors, such as opcode errors, operand errors, and time limits exceeded, ensuring the system handles exceptions gracefully.
- **Job Execution Management**: Executes jobs based on instructions read from an input file. It includes handling read and write operations, tracking execution progress and logging system status.
- **Instruction Processing**: The system uses an instruction register (IR) to process commands and an execution cycle that includes instruction fetching, decoding and executing.
- **Control Cards Handling**: Recognizes control cards such as `$AMJ` for job initialization, `$DTA` for data transfer, and `$END` for termination, allowing for structured job management.
- **System Status Reporting**: Logs detailed information about the current job status, including job ID, instruction counter (IC) and execution metrics like time limit and line limit, to provide insights into job execution and system performance.

## Getting Started

To get started with this project:
1. Clone the repository: `git clone https://github.com/AtharvaKulkarniIT/Multiprogramming-OS.git`
2. Navigate to the project directory: `cd Multiprogramming-OS-main`
3. Compile and run the java programs.

### Input and Output
- **Input File**: The input file should contain commands structured with control cards (`$AMJ`, `$DTA`, `$END`) and data to be processed.
- **Output File**: The output file will store results, system status reports and any error messages generated during execution.

## License 📜

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

Happy coding! 🚀
