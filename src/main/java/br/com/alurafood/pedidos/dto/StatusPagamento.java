package br.com.alurafood.pedidos.dto;

public enum StatusPagamento {
        CRIADO {
            public boolean proximoStatusValido(StatusPagamento status) {
                return status == StatusPagamento.CONFIRMADO || status == StatusPagamento.AGUARDANDO || status == CANCELADO ;
            }
        },
        AGUARDANDO {
            public boolean proximoStatusValido(StatusPagamento status) {
                return status == StatusPagamento.CONFIRMADO || status == CANCELADO ;
            }
        }
        ,
        CONFIRMADO {
            public boolean proximoStatusValido(StatusPagamento status) {
                return false ;
            }
        },
        CANCELADO {
            public boolean proximoStatusValido(StatusPagamento status) {
                return false;
            }
        };

        public boolean validaProximoStatus(StatusPagamento novo) {
            if (this == novo) {
                return true;
            }
            return proximoStatusValido(novo);
        }

        public abstract boolean proximoStatusValido(StatusPagamento status);
    }