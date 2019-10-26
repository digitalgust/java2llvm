#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

#include "runner.h"

void classes_clinit();

void test_Test_main();

void print_debug(int v) {
    printf("lldebug: %d \n", v);
}

void print_ptr(long long v) {
    printf("ll_ptr: %llx (%lld)\n", v, v);
}

typedef union _SlotEntry{
    char ch;
    short s16;
    unsigned short u16;
    int s32;
    unsigned int u32;
    long long s64;
    unsigned long long u64;
    float f32;
    double f64;
    void *_ptr;
}SlotEntry;

int main() {
    SlotEntry slot;
    slot.s32=40;
    print_ptr(slot.s64);
    classes_clinit();
    test_Test_main();
}

/*
 inline s32 stack_size(RuntimeStack *stack) {
    return (stack->sp - stack->store);
}


 inline void push_int(RuntimeStack *stack, s32 value) {
    stack->sp->ivalue = value;//clear 64bit
    stack->sp->type = STACK_ENTRY_INT;
    stack->sp++;
}

 inline s32 pop_int(RuntimeStack *stack) {
    stack->sp--;
    return stack->sp->ivalue;
}


 inline void push_double(RuntimeStack *stack, f64 value) {
    stack->sp->dvalue = value;
    stack->sp->type = STACK_ENTRY_DOUBLE;
    stack->sp++;
    stack->sp->type = STACK_ENTRY_DOUBLE;
//    ptr->dvalue = value;
    stack->sp++;
}

 inline f64 pop_double(RuntimeStack *stack) {
    stack->sp -= 2;
    return stack->sp->dvalue;
}

 inline void push_float(RuntimeStack *stack, f32 value) {
    //ptr->lvalue = 0;//clear 64bit
    stack->sp->fvalue = value;
    stack->sp->type = STACK_ENTRY_FLOAT;
    stack->sp++;
}

 inline f32 pop_float(RuntimeStack *stack) {
    stack->sp--;
    return stack->sp->fvalue;
}


 inline void push_long(RuntimeStack *stack, s64 value) {
    stack->sp->lvalue = value;
    stack->sp->type = STACK_ENTRY_LONG;
    stack->sp++;
    stack->sp->type = STACK_ENTRY_LONG;
//    ptr->lvalue = value;
    stack->sp++;
}

 inline s64 pop_long(RuntimeStack *stack) {
    stack->sp -= 2;
    return stack->sp->lvalue;
}

 inline void push_ref(RuntimeStack *stack, __refer value) {
    stack->sp->type = STACK_ENTRY_REF;
    stack->sp->rvalue = value;
    stack->sp++;
}

 inline __refer pop_ref(RuntimeStack *stack) {
    stack->sp--;
    return stack->sp->rvalue;
}


 inline void push_entry(RuntimeStack *stack, StackEntry *entry) {
    stack->sp->lvalue = entry->lvalue;
    stack->sp->type = entry->type;
    stack->sp++;
}

 inline void pop_entry(RuntimeStack *stack, StackEntry *entry) {
    stack->sp--;
    entry->lvalue = stack->sp->lvalue;
    entry->type = stack->sp->type;

}

 inline void pop_empty(RuntimeStack *stack) {
    stack->sp--;
}


 inline void peek_entry(StackEntry *src, StackEntry *dst) {
    dst->lvalue = src->lvalue;
    dst->type = src->type;
}


 inline s32 entry_2_int(StackEntry *entry) {
    return entry->ivalue;
}

 inline s64 entry_2_long(StackEntry *entry) {
    return entry->lvalue;
}

 inline __refer entry_2_refer(StackEntry *entry) {
    return entry->rvalue;
}
*/