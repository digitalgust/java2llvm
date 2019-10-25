#ifndef RUNNER_H
#define RUNNER_H

/*
#define STACK_ENTRY_NONE        0
#define STACK_ENTRY_INT         1
#define STACK_ENTRY_FLOAT       2
#define STACK_ENTRY_LONG        4
#define STACK_ENTRY_DOUBLE      8
#define STACK_ENTRY_REF         16

typedef unsigned char u8;
typedef signed char s8;
typedef char c8;
typedef unsigned short int u16;
typedef signed short int s16;
typedef unsigned int u32;
typedef signed int s32;
typedef float f32;
typedef double f64;
typedef unsigned long long int u64;
typedef signed long long int s64;
typedef void *__refer;

typedef struct _StackEntry {
    union {
        s64 lvalue;
        f64 dvalue;
        f32 fvalue;
        s32 ivalue;
        __refer rvalue;
    };
    s32 type;
} StackEntry, LocalVarItem;

typedef struct _StackFrame {
    StackEntry *store;
    StackEntry *sp;
    s32 max_size;
}RuntimeStack;


void push_entry(RuntimeStack *stack, StackEntry *entry);

void push_int(RuntimeStack *stack, s32 value);

void push_long(RuntimeStack *stack, s64 value);

void push_double(RuntimeStack *stack, f64 value);

void push_float(RuntimeStack *stack, f32 value);

void push_ref(RuntimeStack *stack, __refer value);

__refer pop_ref(RuntimeStack *stack);

s32 pop_int(RuntimeStack *stack);

s64 pop_long(RuntimeStack *stack);

f64 pop_double(RuntimeStack *stack);

f32 pop_float(RuntimeStack *stack);

void pop_entry(RuntimeStack *stack, StackEntry *entry);

void pop_empty(RuntimeStack *stack);

s32 entry_2_int(StackEntry *entry);

void peek_entry(StackEntry *src, StackEntry *dst);

s64 entry_2_long(StackEntry *entry);

__refer entry_2_refer(StackEntry *entry);

*/
#endif //RUNNER_H