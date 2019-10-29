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


int main() {
    classes_clinit();
    test_Test_main();
}
