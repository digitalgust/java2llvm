#ifndef RUNNER_H
#define RUNNER_H

/*
#define STACK_ENTRY_NONE        0
#define STACK_ENTRY_INT         1
#define STACK_ENTRY_FLOAT       2
#define STACK_ENTRY_LONG        4
#define STACK_ENTRY_DOUBLE      8
#define STACK_ENTRY_REF         16

*/

union StackEntry {
    int s32;
    long long s64;
    float f32;
    double f64;
    void *_ptr;
};

struct StackFrame {
    union StackEntry *store;
    union StackEntry *sp;
    int max_size;
};

extern int max_stack_size;
extern struct StackFrame *pthd_stack;


union StackEntry *get_sp(struct StackFrame *stack);

void set_sp(struct StackFrame *stack, union StackEntry *sp);

void chg_sp(struct StackFrame *stack, int i);

union StackEntry *get_store(struct StackFrame *stack);

int get_stack_size(struct StackFrame *stack);

void push_i64(struct StackFrame *stack, long long i);

long long pop_i64(struct StackFrame *stack);

void push_i32(struct StackFrame *stack, int i);

int pop_i32(struct StackFrame *stack);

void push_i16(struct StackFrame *stack, short i);

short pop_i16(struct StackFrame *stack);

void push_u16(struct StackFrame *stack, unsigned short i);

unsigned short pop_u16(struct StackFrame *stack);

void push_i8(struct StackFrame *stack, char i);

char pop_i8(struct StackFrame *stack);

void push_double(struct StackFrame *stack, double i);

double pop_double(struct StackFrame *stack);

void push_float(struct StackFrame *stack, float i);

float pop_float(struct StackFrame *stack);

void push_ptr(struct StackFrame *stack, void *i);

void *pop_ptr(struct StackFrame *stack);

void push_entry(struct StackFrame *stack, long long i);

long long pop_entry(struct StackFrame *stack);

//====================== local var =====================

void localvar_set_i64(union StackEntry *base, int slot, long long i);

long long localvar_get_i64(union StackEntry *base, int slot);

void localvar_set_i32(union StackEntry *base, int slot, int i);

int localvar_get_i32(union StackEntry *base, int slot);

void localvar_set_i16(union StackEntry *base, int slot, short i);

short localvar_get_i16(union StackEntry *base, int slot);

void localvar_set_u16(union StackEntry *base, int slot, unsigned short i);

unsigned short localvar_get_u16(union StackEntry *base, int slot);

void localvar_set_i8(union StackEntry *base, int slot, char i);

char localvar_get_i8(union StackEntry *base, int slot);

void localvar_set_double(union StackEntry *base, int slot, double i);

double localvar_get_double(union StackEntry *base, int slot);

void localvar_set_float(union StackEntry *base, int slot, float i);

float localvar_get_float(union StackEntry *base, int slot);

void localvar_set_ptr(union StackEntry *base, int slot, void *i);

void *localvar_get_ptr(union StackEntry *base, int slot);

#endif //RUNNER_H