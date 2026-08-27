import React, { useState } from 'react';
import { CopilotResponse } from '../types/copilot';
import { useTranslation } from '../i18n/useTranslation';
import { copilotService } from '../services/copilotService';
import { Bot, Sparkles, ShieldAlert, BookOpen, Send, Zap } from 'lucide-react';

export const CopilotPanel: React.FC = () => {
  const { t } = useTranslation();
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [copilotResponse, setCopilotResponse] = useState<CopilotResponse | null>(null);

  const handleAskCopilot = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setCopilotResponse(null);

    try {
      const res = await copilotService.queryCopilot({ query: query.trim() });
      setCopilotResponse(res);
    } catch (err: any) {
      setCopilotResponse({
        answer: err.message || 'Error al consultar al Copiloto',
        citations: [],
        tokensUsed: 0,
        isRefusedDueToPermissionsOrContext: true,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <aside style={styles.container}>
      {/* Header del Panel del Copiloto */}
      <div style={styles.header}>
        <div style={styles.titleWrapper}>
          <Bot size={22} color="var(--color-primary)" />
          <div>
            <h3 style={styles.title}>{t('copilot_title')}</h3>
            <p style={styles.subtitle}>{t('copilot_subtitle')}</p>
          </div>
        </div>
      </div>

      {/* Formulario de Pregunta */}
      <form onSubmit={handleAskCopilot} style={styles.form}>
        <textarea
          rows={3}
          placeholder={t('copilot_placeholder')}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={styles.textarea}
        />
        <button type="submit" style={styles.askBtn} disabled={loading || !query.trim()}>
          {loading ? (
            'Procesando...'
          ) : (
            <>
              <Sparkles size={16} /> {t('copilot_ask')}
            </>
          )}
        </button>
      </form>

      {/* Area de Resultados / Respuestas */}
      <div style={styles.resultBox}>
        {loading && (
          <div style={styles.loadingBox}>
            <Sparkles size={24} color="var(--color-primary)" style={{ animation: 'spin 2s linear infinite' }} />
            <p>Consultando contexto autorizado en PostgreSQL con RLS y Gemini...</p>
          </div>
        )}

        {copilotResponse && (
          <div style={styles.responseContainer}>
            {/* Alerta de Negativa Explícita (Requisito PDF Sección 8) */}
            {copilotResponse.isRefusedDueToPermissionsOrContext ? (
              <div style={styles.refusalCard}>
                <div style={styles.refusalHeader}>
                  <ShieldAlert size={20} color="#DC2626" />
                  <h4>{t('copilot_refused_title')}</h4>
                </div>
                <p style={styles.refusalText}>{copilotResponse.answer}</p>
              </div>
            ) : (
              <div style={styles.answerCard}>
                <div style={styles.answerHeader}>
                  <Bot size={18} color="var(--color-primary)" />
                  <h4>Respuesta del Copiloto (Gemini 3.6 Flash)</h4>
                </div>
                <p style={styles.answerText}>{copilotResponse.answer}</p>
              </div>
            )}

            {/* Citas a Mensajes Fuente (Requisito PDF Sección 8) */}
            {copilotResponse.citations && copilotResponse.citations.length > 0 && (
              <div style={styles.citationsSection}>
                <div style={styles.citationsTitle}>
                  <BookOpen size={16} color="var(--color-text-muted)" />
                  <h5>{t('copilot_citations')}</h5>
                </div>
                <div style={styles.citationsList}>
                  {copilotResponse.citations.map((cit, idx) => (
                    <div key={idx} style={styles.citationCard}>
                      <div style={styles.citationMeta}>
                        <span style={styles.citChannel}>#{cit.channelName}</span>
                        <span style={styles.citSender}>— {cit.senderName}</span>
                      </div>
                      <p style={styles.citSnippet}>"{cit.contentSnippet}"</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Métrica de Auditoría de Tokens */}
            <div style={styles.tokenFooter}>
              <Zap size={14} color="#F59E0B" />
              <span>{t('copilot_tokens')} <strong>{copilotResponse.tokensUsed}</strong></span>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: {
    width: '340px',
    backgroundColor: 'var(--color-surface)',
    borderLeft: '1px solid var(--color-border)',
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
  },
  header: {
    padding: '16px',
    borderBottom: '1px solid var(--color-border-light)',
    backgroundColor: '#FFFFFF',
  },
  titleWrapper: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  title: {
    fontSize: '15px',
    color: 'var(--color-header-bg)',
    fontWeight: 600,
  },
  subtitle: {
    fontSize: '11px',
    color: 'var(--color-text-muted)',
  },
  form: {
    padding: '16px',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    backgroundColor: '#FFFFFF',
    borderBottom: '1px solid var(--color-border-light)',
  },
  textarea: {
    resize: 'none',
    fontSize: '13px',
    width: '100%',
  },
  askBtn: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    padding: '10px',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: 600,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
  },
  resultBox: {
    flex: 1,
    overflowY: 'auto',
    padding: '16px',
  },
  loadingBox: {
    textAlign: 'center',
    padding: '30px 10px',
    color: 'var(--color-text-muted)',
    fontSize: '13px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: '12px',
  },
  responseContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  refusalCard: {
    backgroundColor: '#FEE2E2',
    border: '1px solid #FCA5A5',
    borderRadius: '8px',
    padding: '14px',
  },
  refusalHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    color: '#DC2626',
    marginBottom: '6px',
  },
  refusalText: {
    fontSize: '13px',
    color: '#991B1B',
    lineHeight: '1.4',
  },
  answerCard: {
    backgroundColor: '#FFFFFF',
    border: '1px solid var(--color-border)',
    borderRadius: '8px',
    padding: '14px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
  },
  answerHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '8px',
    color: 'var(--color-header-bg)',
  },
  answerText: {
    fontSize: '13px',
    lineHeight: '1.5',
    color: 'var(--color-text-main)',
    whiteSpace: 'pre-line',
  },
  citationsSection: {
    backgroundColor: 'var(--color-surface-alt)',
    borderRadius: '8px',
    padding: '12px',
  },
  citationsTitle: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    marginBottom: '8px',
    fontSize: '12px',
    color: 'var(--color-text-muted)',
  },
  citationsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  citationCard: {
    backgroundColor: '#FFFFFF',
    padding: '8px 10px',
    borderRadius: '6px',
    border: '1px solid var(--color-border-light)',
  },
  citationMeta: {
    display: 'flex',
    gap: '6px',
    fontSize: '11px',
    marginBottom: '2px',
  },
  citChannel: {
    fontWeight: 'bold',
    color: 'var(--color-primary)',
  },
  citSender: {
    color: 'var(--color-text-muted)',
  },
  citSnippet: {
    fontSize: '12px',
    fontStyle: 'italic',
    color: 'var(--color-text-main)',
  },
  tokenFooter: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '11px',
    color: 'var(--color-text-muted)',
    alignSelf: 'flex-end',
  },
};
