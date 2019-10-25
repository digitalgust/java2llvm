#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>

int ptr_size() {
    return sizeof(int *);
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

void java_io_PrintStream_print_C(void *ps, unsigned short value) {
    printf("%c", (char) value);
}