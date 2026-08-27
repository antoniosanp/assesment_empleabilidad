import { useState, useEffect } from 'react';
import es from './es.json';
import en from './en.json';

export type Language = 'es' | 'en';

const translations: Record<Language, Record<string, string>> = { es, en };

export function useTranslation() {
  const [lang, setLang] = useState<Language>(() => {
    return (localStorage.getItem('riwi_lang') as Language) || 'es';
  });

  useEffect(() => {
    localStorage.setItem('riwi_lang', lang);
  }, [lang]);

  const t = (key: string): string => {
    return translations[lang][key] || key;
  };

  return { t, lang, setLang };
}
