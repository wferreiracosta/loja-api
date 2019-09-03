package br.com.wferreiracosta.loja.services;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.wferreiracosta.loja.domain.ItemPedido;
import br.com.wferreiracosta.loja.domain.PagamentoComBoleto;
import br.com.wferreiracosta.loja.domain.Pedido;
import br.com.wferreiracosta.loja.domain.enums.EstadoPagamento;
import br.com.wferreiracosta.loja.repositories.ItemPedidoRepository;
import br.com.wferreiracosta.loja.repositories.PagamentoRepository;
import br.com.wferreiracosta.loja.repositories.PedidoRepository;
import br.com.wferreiracosta.loja.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	@Autowired
	private PedidoRepository repo;

	@Autowired
	private PagamentoRepository repoPagamento;

	@Autowired
	private ItemPedidoRepository repoItemPedido;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private BoletoService boletoService;

	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}

	@Transactional
	public Pedido insert(@Valid Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, new Date());
		}
		obj = repo.save(obj);
		repoPagamento.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setPreco(produtoService.find(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		repoItemPedido.saveAll(obj.getItens());
		System.out.println(obj);
		return obj;
	}

}