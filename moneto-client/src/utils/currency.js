export const getCurrencySymbol = (currency) => {
  const symbols = {
    USD: '$',
    EUR: '€',
    GBP: '£',
    ALL: 'L',
    CHF: 'CHF',
    CAD: 'CA$',
    AUD: 'A$',
    JPY: '¥'
  }
  return symbols[currency] || '$'
}