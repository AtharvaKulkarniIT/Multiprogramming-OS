# Multiprogramming Operating System

Welcome to the **Multiprogramming Operating System** project! 🎉 This project simulates a multiprogramming environment, implementing various phases of execution and management. Below, you'll find an overview of the three phases and their functionalities.

## Phases Overview

### Phase 1: Instruction Processing 🧩

**Description**: In this phase, the system processes instructions and handles basic memory operations. It reads instructions from an input file, executes them and writes results to an output file.

**Key Features**:
- **Instruction Register (IR)**: Stores the current instruction being executed.
- **Memory Management (M)**: A simulated memory array used for storing and manipulating data.
- **File Operations**: Manages reading from an input file and writing results to an output file.

### Phase 2: Memory and Job Management 🗃️

**Description**: This phase builds on Phase 1 by integrating advanced memory management and job handling techniques. It includes dynamic memory allocation, error handling and job execution management to simulate a more realistic operating system environment.

**Key Features**:
- **Dynamic Memory Allocation**: Allocates memory dynamically based on job requirements, simulating page faults and memory allocation strategies.
- **Error Handling**: Detects and manages various errors, such as opcode errors, operand errors, and page faults, ensuring the system handles exceptions gracefully.
- **Job Execution Management**: Executes jobs based on instructions, including handling read and write operations, managing timers and tracking execution progress.
- **System Status Reporting**: Logs detailed information about system status, including timers, line counts and error codes, to provide insights into job execution and system performance.

### Phase 3: Advanced Job Handling 🛠️

**Description**: The final phase introduces more sophisticated job handling mechanisms, including multi-job processing and improved data management. This phase ensures smooth execution of multiple jobs with efficient resource utilization.

**Key Features**:
- **Multi-Job Processing**: Supports running multiple jobs concurrently.
- **Enhanced Data Management**: Improves handling of data and job instructions for better performance and reliability.

## Getting Started

To get started with this project:
1. Clone the repository: `git clone https://github.com/AtharvaKulkarniIT/Multiprogramming-OS.git`
2. Navigate to the project directory: `cd Multiprogramming-OS-main`
3. Compile and run the program


## License 📜

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

Happy coding! 🚀
