#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

#include "runner.h"

int max_stack_size = 20480;
struct StackFrame *pthd_stack;  //can be extends to multithread

void classes_clinit();

void test_Test_main();

void print_debug(int v) {
    printf("lldebug: %d \n", v);
}

void print_ptr(char *v) {
    printf("ll_ptr: %llx (%lld)\n", (long long)(intptr_t)v, (long long)(intptr_t)v);
}

int main() {
    pthd_stack = malloc(sizeof(struct StackFrame));
    pthd_stack->store = malloc(sizeof(union StackEntry) * max_stack_size);
    pthd_stack->sp = pthd_stack->store;

    classes_clinit();
    test_Test_main();

    free(pthd_stack);
}

