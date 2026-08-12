// snippets/node/controller.ts
// Controller NestJS — ver recipes/node-create-rest-endpoint.md.

@Controller('api/v1/pedidos')
export class PedidoController {
  constructor(
    private readonly crearPedidoUseCase: CrearPedidoUseCase,
    private readonly pedidoMapper: PedidoMapper,
  ) {}

  @Post()
  async crear(@Body() dto: CrearPedidoDto): Promise<PedidoResponseDto> {
    const lineas = dto.lineas.map(l => this.pedidoMapper.aLineaPedido(l));
    const pedido = await this.crearPedidoUseCase.ejecutar(dto.clienteId, lineas);
    return this.pedidoMapper.aResponse(pedido);
  }

  @Get(':id')
  async obtener(@Param('id', ParseUUIDPipe) id: string): Promise<PedidoResponseDto> {
    const pedido = await this.buscarPedidoUseCase.ejecutar(id);
    return this.pedidoMapper.aResponse(pedido);
  }

  @Get()
  async listar(
    @Query('cursor') cursor?: string,
    @Query('limit', new DefaultValuePipe(20), ParseIntPipe) limit?: number,
  ): Promise<{ items: PedidoResponseDto[]; nextCursor?: string }> {
    const limiteAcotado = Math.min(limit ?? 20, 100);
    const { items, nextCursor } = await this.listarPedidosUseCase.ejecutar({ cursor, limite: limiteAcotado });
    return { items: items.map(p => this.pedidoMapper.aResponse(p)), nextCursor };
  }
}
