package com.jfcdevs.app;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ReactorTests {

    @Test
    void testFlux(){
        List<Integer> list = new ArrayList<>();

        Flux.just(1,2,3,4,5,6)
                .filter(n -> n % 2 == 0)
                .map(n -> n*2)
                .log()
                .subscribe(n -> list.add(n));

        Assertions.assertThat(list).containsExactly(4,8,12);
    }
}
