#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>

#include "runner.h"

int ptr_size() {
    return sizeof(int *);
}

// ====================== stack =====================
union StackEntry *get_sp(struct StackFrame *stack) {
    return stack->sp;
}

void set_sp(struct StackFrame *stack, union StackEntry *sp) {
    stack->sp = sp;
}

void chg_sp(struct StackFrame *stack, int i) {
    stack->sp += i;
}

union StackEntry *get_store(struct StackFrame *stack) {
    return stack->store;
}

int get_stack_size(struct StackFrame *stack) {
    return stack->sp - stack->store;
}

void push_i64(struct StackFrame *stack, long long i) {
    stack->sp->s64 = i;
    stack->sp++;
    stack->sp++;
}

long long pop_i64(struct StackFrame *stack) {
    --stack->sp;
    return (--stack->sp)->s64;
}

void push_i32(struct StackFrame *stack, int i) {
    stack->sp->s32 = i;
    stack->sp++;
}

int pop_i32(struct StackFrame *stack) {
    return (--stack->sp)->s32;
}

void push_i16(struct StackFrame *stack, short i) {
    stack->sp->s32 = i;
    stack->sp++;
}

short pop_i16(struct StackFrame *stack) {
    return (short)(--stack->sp)->s32;
}

void push_u16(struct StackFrame *stack, unsigned short i) {
    stack->sp->s32 = i;
    stack->sp++;
}

unsigned short pop_u16(struct StackFrame *stack) {
    return (unsigned short)(--stack->sp)->s32;
}

void push_i8(struct StackFrame *stack, char i) {
    stack->sp->s32 = i;
    stack->sp++;
}

char pop_i8(struct StackFrame *stack) {
    return (char)(--stack->sp)->s32;
}

void push_double(struct StackFrame *stack, double i) {
    stack->sp->f64 = i;
    stack->sp++;
    stack->sp++;
}

double pop_double(struct StackFrame *stack) {
    --stack->sp;
    return (--stack->sp)->f64;
}

void push_float(struct StackFrame *stack, float i) {
    stack->sp->f32 = i;
    stack->sp++;
}

float pop_float(struct StackFrame *stack) {
    return (--stack->sp)->f32;
}

void push_ptr(struct StackFrame *stack, void *i) {
    stack->sp->_ptr = i;
    stack->sp++;
}

void *pop_ptr(struct StackFrame *stack) {
    return (--stack->sp)->_ptr;
}

void push_entry(struct StackFrame *stack, long long i) {
    stack->sp->s64 = i;
    stack->sp++;
}

long long pop_entry(struct StackFrame *stack) {
    return (--stack->sp)->s64;
}

//====================== local var =====================

void localvar_set_i64(union StackEntry *base, int slot, long long i) {
    (base + slot)->s64 = i;
}

long long localvar_get_i64(union StackEntry *base, int slot) {
    return (base + slot)->s64;
}

void localvar_set_i32(union StackEntry *base, int slot, int i) {
    (base + slot)->s32 = i;
}

int localvar_get_i32(union StackEntry *base, int slot) {
    return (base + slot)->s32;
}

void localvar_set_i16(union StackEntry *base, int slot, short i) {
    (base + slot)->s32 = i;
}

short localvar_get_i16(union StackEntry *base, int slot) {
    return (short)(base + slot)->s32;
}

void localvar_set_u16(union StackEntry *base, int slot, unsigned short i) {
    (base + slot)->s32 = i;
}

unsigned short localvar_get_u16(union StackEntry *base, int slot) {
    return (unsigned short)(base + slot)->s32;
}

void localvar_set_i8(union StackEntry *base, int slot, char i) {
    (base + slot)->s32 = i;
}

char localvar_get_i8(union StackEntry *base, int slot) {
    return (char)(base + slot)->s32;
}

void localvar_set_double(union StackEntry *base, int slot, double i) {
    (base + slot)->f64 = i;
}

double localvar_get_double(union StackEntry *base, int slot) {
    return (base + slot)->f64;
}

void localvar_set_float(union StackEntry *base, int slot, float i) {
    (base + slot)->f32 = i;
}

float localvar_get_float(union StackEntry *base, int slot) {
    return (base + slot)->f32;
}

void localvar_set_ptr(union StackEntry *base, int slot, void *i) {
    (base + slot)->_ptr = i;
}

void *localvar_get_ptr(union StackEntry *base, int slot) {
    return (base + slot)->_ptr;
}


long long java_lang_System_currentTimeMillis() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    long long v = tv.tv_sec * 1000 + tv.tv_usec / 1000;
    return v;
}

long long java_lang_System_nanoTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    long long v = tv.tv_sec * 1000000 + tv.tv_usec;
    v *= 1000;
    return v;
}


void java_io_PrintStream_println_I(void *ps, int value) {
    printf("%d\n", value);
}

void java_io_PrintStream_println_J(void *ps, long long value) {
    printf("%lld\n", value);
}

void java_io_PrintStream_println_D(void *ps, double value) {
    printf("%lf\n", value);
}

void java_io_PrintStream_print_C(void *ps, unsigned short value) {
    printf("%c", (char) value);
}