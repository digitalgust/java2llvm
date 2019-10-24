#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

#include "runner.h"

void test_Test__clinit_();

void test_Test_main();

int main() {
    test_Test__clinit_();
    test_Test_main();
}


 inline s32 stack_size(RuntimeStack *stack) {
    return (stack->sp - stack->store);
}

/* push Integer */
 inline void push_int(RuntimeStack *stack, s32 value) {
    stack->sp->ivalue = value;//clear 64bit
    stack->sp->type = STACK_ENTRY_INT;
    stack->sp++;
}


/* pop Integer */
 inline s32 pop_int(RuntimeStack *stack) {
    stack->sp--;
    return stack->sp->ivalue;
}

/* push Double */
 inline void push_double(RuntimeStack *stack, f64 value) {
    stack->sp->dvalue = value;
    stack->sp->type = STACK_ENTRY_DOUBLE;
    stack->sp++;
    stack->sp->type = STACK_ENTRY_DOUBLE;
//    ptr->dvalue = value;
    stack->sp++;
}

/* pop Double */
 inline f64 pop_double(RuntimeStack *stack) {
    stack->sp -= 2;
    return stack->sp->dvalue;
}

/* push Float */
 inline void push_float(RuntimeStack *stack, f32 value) {
    //ptr->lvalue = 0;//clear 64bit
    stack->sp->fvalue = value;
    stack->sp->type = STACK_ENTRY_FLOAT;
    stack->sp++;
}

/* pop Float */
 inline f32 pop_float(RuntimeStack *stack) {
    stack->sp--;
    return stack->sp->fvalue;
}


/* push Long */
 inline void push_long(RuntimeStack *stack, s64 value) {
    stack->sp->lvalue = value;
    stack->sp->type = STACK_ENTRY_LONG;
    stack->sp++;
    stack->sp->type = STACK_ENTRY_LONG;
//    ptr->lvalue = value;
    stack->sp++;
}

/* pop Long */
 inline s64 pop_long(RuntimeStack *stack) {
    stack->sp -= 2;
    return stack->sp->lvalue;
}

/* push Ref */
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

/* Pop Stack Entry */
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


/* Entry to Int */
 inline s32 entry_2_int(StackEntry *entry) {
    return entry->ivalue;
}

 inline s64 entry_2_long(StackEntry *entry) {
    return entry->lvalue;
}

 inline __refer entry_2_refer(StackEntry *entry) {
    return entry->rvalue;
}
