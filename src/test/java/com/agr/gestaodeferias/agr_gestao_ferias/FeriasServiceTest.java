package com.agr.gestaodeferias.agr_gestao_ferias;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.agr.gestaodeferias.agr_gestao_ferias.models.FeriasPedido;
import com.agr.gestaodeferias.agr_gestao_ferias.models.FuncionarioClt;
import com.agr.gestaodeferias.agr_gestao_ferias.models.Regime;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FeriasPedidoRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.repository.FuncionarioRepository;
import com.agr.gestaodeferias.agr_gestao_ferias.service.FeriasService;

public class FeriasServiceTest {

    @Mock
    private FeriasPedidoRepository feriasPedidoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private FeriasService feriasService;

    private FuncionarioClt funcionario;
    private FeriasPedido pedido;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        funcionario = new FuncionarioClt(
            "João Silva",
            "123.456.789-00",
            Regime.CLT,
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31)
        );
        funcionario.setId(UUID.randomUUID());
        
        pedido = new FeriasPedido();
        pedido.setId(1L);
        pedido.setFuncionario(funcionario);
        pedido.setParcelaNumero(1);
        pedido.setQuantidadeDias(15);
        pedido.setDataInicio(LocalDate.of(2024, 7, 1));
        pedido.setDataFim(LocalDate.of(2024, 7, 15));
    }

    @Test
    void testFuncionarioIniciaCom3ParcelasDisponiveis() {
        // Assert
        assertEquals(3, funcionario.getParcelasDisponiveis());
        assertTrue(funcionario.temParcelasDisponiveis());
    }



    @Test
    void testSaldoRecemAdmitido() {
        // Funcionário admitido hoje
        FuncionarioClt novo = new FuncionarioClt("Novo", "000.000.000-00", Regime.CLT, LocalDate.now(), LocalDate.now().plusYears(1));
        novo.setId(UUID.randomUUID());
        when(feriasPedidoRepository.findByFuncionarioId(novo.getId())).thenReturn(Arrays.asList());
        // Força o cálculo
        // Chama via reflexão pois o método é private
        try {
            java.lang.reflect.Method m = feriasService.getClass().getDeclaredMethod("atualizarDiasDeFeriasAcumulados", com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario.class);
            m.setAccessible(true);
            m.invoke(feriasService, novo);
        } catch (Exception e) { fail(e); }
        assertEquals(0, novo.getNumeroDeDiasDeFeriasAcumulados());
    }

    @Test
    void testSaldoComAnosCompletosSemFérias() {
        // Admitido há 3 anos
        FuncionarioClt antigo = new FuncionarioClt("Antigo", "111.111.111-11", Regime.CLT, LocalDate.now().minusYears(3), LocalDate.now().minusYears(2));
        antigo.setId(UUID.randomUUID());
        when(feriasPedidoRepository.findByFuncionarioId(antigo.getId())).thenReturn(Arrays.asList());
        try {
            java.lang.reflect.Method m = feriasService.getClass().getDeclaredMethod("atualizarDiasDeFeriasAcumulados", com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario.class);
            m.setAccessible(true);
            m.invoke(feriasService, antigo);
        } catch (Exception e) { fail(e); }
        assertEquals(90, antigo.getNumeroDeDiasDeFeriasAcumulados());
    }

    @Test
    void testSaldoComFériasParciais() {
        // Admitido há 2 anos
        LocalDate admissao = LocalDate.now().minusYears(2);
        FuncionarioClt parcial = new FuncionarioClt("Parcial", "222.222.222-22", Regime.CLT, admissao, admissao.plusYears(1));
        parcial.setId(UUID.randomUUID());
        FeriasPedido p1 = new FeriasPedido();
        p1.setFuncionario(parcial);
        p1.setQuantidadeDias(15);
        p1.setDataInicio(admissao.plusMonths(6));
        when(feriasPedidoRepository.findByFuncionarioId(parcial.getId())).thenReturn(Arrays.asList(p1));
        try {
            java.lang.reflect.Method m = feriasService.getClass().getDeclaredMethod("atualizarDiasDeFeriasAcumulados", com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario.class);
            m.setAccessible(true);
            m.invoke(feriasService, parcial);
        } catch (Exception e) { fail(e); }
        assertEquals(45, parcial.getNumeroDeDiasDeFeriasAcumulados());
    }

    @Test
    void testLimiteMaximo90Dias() {
        // Admitido há 10 anos, nunca tirou férias
        FuncionarioClt veterano = new FuncionarioClt("Veterano", "333.333.333-33", Regime.CLT, LocalDate.now().minusYears(10), LocalDate.now().minusYears(9));
        veterano.setId(UUID.randomUUID());
        when(feriasPedidoRepository.findByFuncionarioId(veterano.getId())).thenReturn(Arrays.asList());
        try {
            java.lang.reflect.Method m = feriasService.getClass().getDeclaredMethod("atualizarDiasDeFeriasAcumulados", com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario.class);
            m.setAccessible(true);
            m.invoke(feriasService, veterano);
        } catch (Exception e) { fail(e); }
        assertEquals(90, veterano.getNumeroDeDiasDeFeriasAcumulados());
    }

    @Test
    void testAcumuloNoAniversarioDeAdmissao() {
        // Admissão há 1 ano e 1 dia (para garantir que já passou do aniversário)
        LocalDate admissao = LocalDate.now().minusYears(1).minusDays(1);

        // Funcionário que já passou do primeiro aniversário (saldo deve ser 30)
        FuncionarioClt funcionarioDepois = new FuncionarioClt("Teste", "999.999.999-99", Regime.CLT, admissao, admissao.plusYears(1));
        funcionarioDepois.setId(UUID.randomUUID());
        when(feriasPedidoRepository.findByFuncionarioId(funcionarioDepois.getId())).thenReturn(Arrays.asList());
        try {
            java.lang.reflect.Method m = feriasService.getClass().getDeclaredMethod("atualizarDiasDeFeriasAcumulados", com.agr.gestaodeferias.agr_gestao_ferias.models.Funcionario.class);
            m.setAccessible(true);
            m.invoke(feriasService, funcionarioDepois);
        } catch (Exception e) { fail(e); }
        assertEquals(30, funcionarioDepois.getNumeroDeDiasDeFeriasAcumulados());
    }

    @Test
    void testResetParcelasNoNovoCicloAnual() {
        // Admissão em 10/03/2020
        LocalDate admissao = LocalDate.of(2020, 3, 10);
        FuncionarioClt funcionario = new FuncionarioClt("Teste Reset", "999.999.999-99", Regime.CLT, admissao, admissao.plusYears(1));
        funcionario.setId(UUID.randomUUID());

        // 3 pedidos no ciclo 2023-2024 (de 10/03/2023 a 09/03/2024)
        FeriasPedido p1 = new FeriasPedido();
        p1.setFuncionario(funcionario);
        p1.setParcelaNumero(1);
        p1.setQuantidadeDias(15);
        p1.setDataInicio(LocalDate.of(2023, 4, 1));

        FeriasPedido p2 = new FeriasPedido();
        p2.setFuncionario(funcionario);
        p2.setParcelaNumero(2);
        p2.setQuantidadeDias(10);
        p2.setDataInicio(LocalDate.of(2023, 7, 1));

        FeriasPedido p3 = new FeriasPedido();
        p3.setFuncionario(funcionario);
        p3.setParcelaNumero(3);
        p3.setQuantidadeDias(10);
        p3.setDataInicio(LocalDate.of(2023, 12, 1));

        // Mocka os pedidos já feitos
        when(feriasPedidoRepository.findByFuncionarioId(funcionario.getId())).thenReturn(Arrays.asList(p1, p2, p3));

        // Novo pedido no mesmo ciclo (deve lançar exceção)
        FeriasPedido novoPedidoMesmoCiclo = new FeriasPedido();
        novoPedidoMesmoCiclo.setFuncionario(funcionario);
        novoPedidoMesmoCiclo.setParcelaNumero(1);
        novoPedidoMesmoCiclo.setQuantidadeDias(5);
        novoPedidoMesmoCiclo.setDataInicio(LocalDate.of(2024, 2, 1)); // Ainda dentro do ciclo 2023-2024

        assertThrows(IllegalArgumentException.class, () -> {
            feriasService.solicitarFerias(novoPedidoMesmoCiclo);
        }, "Deveria bloquear mais de 3 parcelas no mesmo ciclo anual");

        // Novo pedido no ciclo seguinte (após 10/03/2024)
        FeriasPedido novoPedidoNovoCiclo = new FeriasPedido();
        novoPedidoNovoCiclo.setFuncionario(funcionario);
        novoPedidoNovoCiclo.setParcelaNumero(1);
        novoPedidoNovoCiclo.setQuantidadeDias(15);
        novoPedidoNovoCiclo.setDataInicio(LocalDate.of(2024, 3, 15)); // Novo ciclo

        // Mocka que não há pedidos aprovados no novo ciclo
        when(feriasPedidoRepository.findByFuncionarioId(funcionario.getId())).thenReturn(Arrays.asList());

        // Não deve lançar exceção
        try {
            feriasService.solicitarFerias(novoPedidoNovoCiclo);
        } catch (Exception e) {
            fail("Não deveria bloquear pedido no novo ciclo anual: " + e.getMessage());
        }
    }

    @Test
    void testNaoPermiteMaisDe30DiasNoMesmoCicloAnual() {
        // Admissão em 10/03/2020
        LocalDate admissao = LocalDate.of(2020, 3, 10);
        FuncionarioClt funcionario = new FuncionarioClt("Teste Limite Dias", "888.888.888-88", Regime.CLT, admissao, admissao.plusYears(1));
        funcionario.setId(UUID.randomUUID());

        // 2 pedidos já aprovados no ciclo: 15 + 10 = 25 dias
        FeriasPedido p1 = new FeriasPedido();
        p1.setFuncionario(funcionario);
        p1.setParcelaNumero(1);
        p1.setQuantidadeDias(15);
        p1.setDataInicio(LocalDate.of(2023, 4, 1));

        FeriasPedido p2 = new FeriasPedido();
        p2.setFuncionario(funcionario);
        p2.setParcelaNumero(2);
        p2.setQuantidadeDias(10);
        p2.setDataInicio(LocalDate.of(2023, 7, 1));

        // Mocka os pedidos já feitos
        when(feriasPedidoRepository.findByFuncionarioId(funcionario.getId())).thenReturn(Arrays.asList(p1, p2));

        // Novo pedido que ultrapassa o limite (10 dias, totalizaria 35)
        FeriasPedido p3 = new FeriasPedido();
        p3.setFuncionario(funcionario);
        p3.setParcelaNumero(3);
        p3.setQuantidadeDias(10);
        p3.setDataInicio(LocalDate.of(2023, 12, 1));

        assertThrows(IllegalArgumentException.class, () -> {
            feriasService.solicitarFerias(p3);
        }, "Deveria bloquear pedido que ultrapassa 30 dias no mesmo ciclo anual");
    }
} 