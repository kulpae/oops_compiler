
interface Inspector {
    
    void sendRegister(int index, int value);
    void sendMemory(int index, int value);
    void sendException(Exception e);
    void setupMemory(int memory[]);
    void setupRegisters(int registers[]);
    void nextStep();
    void setStepByStep(boolean value);
    void readCode(String fileName);
}
