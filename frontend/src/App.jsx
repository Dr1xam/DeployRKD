import React, { useState, useEffect } from 'react'
import {
  FileText,
  FileSpreadsheet,
  Mail,
  Plus,
  Filter,
  RefreshCw,
  Trash2,
  TrendingUp,
  DollarSign,
  ShoppingBag,
  Users,
  MapPin,
  Calendar,
  CheckCircle2,
  AlertCircle,
  X,
  Search,
  Download,
  Send,
  Database,
  Layers
} from 'lucide-react'

const API_BASE = window.location.port === '5173' ? 'http://localhost:8080' : ''

const REGIONS = ['Київ', 'Західний', 'Центральний', 'Південний', 'Східний']

export default function App() {
  const [sales, setSales] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  // Filter States
  const [selectedMonth, setSelectedMonth] = useState('')
  const [selectedRegion, setSelectedRegion] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  // Modals
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isEmailModalOpen, setIsEmailModalOpen] = useState(false)

  // Forms
  const [newSale, setNewSale] = useState({
    manager: '',
    product: '',
    amount: '',
    date: new Date().toISOString().split('T')[0],
    region: 'Київ'
  })

  const [emailForm, setEmailForm] = useState({
    emails: 'ceo@company.com, sales-head@company.com',
    month: '',
    from: '',
    to: '',
    region: ''
  })

  // Notifications
  const [toast, setToast] = useState(null)
  const [emailLoading, setEmailLoading] = useState(false)

  const showToast = (message, type = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 5000)
  }

  // Load Data
  const fetchData = async () => {
    setLoading(true)
    try {
      const params = new URLSearchParams()
      if (selectedMonth) params.append('month', selectedMonth)
      if (selectedRegion) params.append('region', selectedRegion)
      if (fromDate) params.append('from', fromDate)
      if (toDate) params.append('to', toDate)

      const [salesRes, summaryRes] = await Promise.all([
        fetch(`${API_BASE}/sales?${params.toString()}`),
        fetch(`${API_BASE}/reports/summary?${params.toString()}`)
      ])

      if (salesRes.ok && summaryRes.ok) {
        const salesData = await salesRes.json()
        const summaryData = await summaryRes.json()
        setSales(salesData)
        setSummary(summaryData)
      } else {
        showToast('Помилка завантаження даних із сервера', 'error')
      }
    } catch (err) {
      console.error(err)
      showToast('Не вдалося з’єднатися з сервером backend', 'error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [selectedMonth, selectedRegion, fromDate, toDate])

  // Handlers
  const handleAddSale = async (e) => {
    e.preventDefault()
    if (!newSale.manager || !newSale.product || !newSale.amount || !newSale.date || !newSale.region) {
      showToast('Будь ласка, заповніть усі поля', 'error')
      return
    }

    try {
      const res = await fetch(`${API_BASE}/sales`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...newSale,
          amount: parseFloat(newSale.amount)
        })
      })

      if (res.ok) {
        showToast('Продаж успішно додано!')
        setIsAddModalOpen(false)
        setNewSale({
          manager: '',
          product: '',
          amount: '',
          date: new Date().toISOString().split('T')[0],
          region: 'Київ'
        })
        fetchData()
      } else {
        const err = await res.json()
        showToast(err.message || 'Помилка при додаванні продажу', 'error')
      }
    } catch (err) {
      showToast('Помилка сервера: ' + err.message, 'error')
    }
  }

  const handleDeleteSale = async (id) => {
    if (!window.confirm('Ви дійсно бажаєте видалити цей запис?')) return
    try {
      const res = await fetch(`${API_BASE}/sales/${id}`, { method: 'DELETE' })
      if (res.ok) {
        showToast('Запис продажу видалено')
        fetchData()
      } else {
        showToast('Не вдалося видалити запис', 'error')
      }
    } catch (err) {
      showToast('Помилка: ' + err.message, 'error')
    }
  }

  const handleDownloadPdf = () => {
    const params = new URLSearchParams()
    if (selectedMonth) params.append('month', selectedMonth)
    if (selectedRegion) params.append('region', selectedRegion)
    if (fromDate) params.append('from', fromDate)
    if (toDate) params.append('to', toDate)
    window.open(`${API_BASE}/reports/sales.pdf?${params.toString()}`, '_blank')
  }

  const handleDownloadExcel = () => {
    const params = new URLSearchParams()
    if (selectedMonth) params.append('month', selectedMonth)
    if (selectedRegion) params.append('region', selectedRegion)
    if (fromDate) params.append('from', fromDate)
    if (toDate) params.append('to', toDate)
    window.location.href = `${API_BASE}/reports/sales.xlsx?${params.toString()}`
  }

  const handleSendEmail = async (e) => {
    e.preventDefault()
    setEmailLoading(true)

    const emailList = emailForm.emails
      .split(',')
      .map(e => e.trim())
      .filter(Boolean)

    if (emailList.length === 0) {
      showToast('Введіть хоча б одну адресу email', 'error')
      setEmailLoading(false)
      return
    }

    try {
      const res = await fetch(`${API_BASE}/reports/send`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          emails: emailList,
          month: emailForm.month || selectedMonth || undefined,
          from: emailForm.from || fromDate || undefined,
          to: emailForm.to || toDate || undefined,
          region: emailForm.region || selectedRegion || undefined
        })
      })

      const data = await res.json()
      if (data.success) {
        showToast(`✅ ${data.message}`)
        setIsEmailModalOpen(false)
      } else {
        showToast(`⚠️ ${data.message}`, 'error')
      }
    } catch (err) {
      showToast('Помилка відправки: ' + err.message, 'error')
    } finally {
      setEmailLoading(false)
    }
  }

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '0 ₴'
    return new Intl.NumberFormat('uk-UA', { style: 'currency', currency: 'UAH' }).format(val)
  }

  const filteredSales = sales.filter(s => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    return (
      s.manager.toLowerCase().includes(term) ||
      s.product.toLowerCase().includes(term) ||
      s.region.toLowerCase().includes(term)
    )
  })

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col antialiased">
      {/* Toast Notification */}
      {toast && (
        <div className={`fixed top-5 right-5 z-50 flex items-center gap-3 px-5 py-3.5 rounded-xl shadow-xl border text-sm font-medium transition-all transform animate-bounce ${
          toast.type === 'error'
            ? 'bg-rose-50 text-rose-800 border-rose-200'
            : 'bg-emerald-50 text-emerald-800 border-emerald-200'
        }`}>
          {toast.type === 'error' ? <AlertCircle className="w-5 h-5 text-rose-600" /> : <CheckCircle2 className="w-5 h-5 text-emerald-600" />}
          <span>{toast.message}</span>
          <button onClick={() => setToast(null)} className="ml-2 text-slate-400 hover:text-slate-700">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Top Navbar */}
      <header className="border-b border-slate-200 bg-white/90 backdrop-blur-md sticky top-0 z-30 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center shadow-md shadow-blue-500/20 text-white">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-lg text-slate-900 tracking-tight">DeeployRKD</span>
                {summary && (
                  <span className={`text-[11px] px-2.5 py-0.5 rounded-full font-medium border flex items-center gap-1.5 ${
                    summary.storageType === 'PostgreSQL'
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-300'
                      : 'bg-amber-50 text-amber-800 border-amber-300'
                  }`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${summary.storageType === 'PostgreSQL' ? 'bg-emerald-500' : 'bg-amber-500'}`}></span>
                    <span>{summary.storageType || 'InMemory'}</span>
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500">Генератор PDF-звітів з Email-розсилкою</p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2.5">
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition shadow-xs cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Додати продаж</span>
            </button>

            <button
              onClick={handleDownloadPdf}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-lg text-sm font-medium transition cursor-pointer"
              title="Завантажити PDF звіт з кирилицею та графіками"
            >
              <FileText className="w-4 h-4 text-rose-600" />
              <span className="hidden sm:inline">PDF-звіт</span>
            </button>

            <button
              onClick={handleDownloadExcel}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 rounded-lg text-sm font-medium transition cursor-pointer"
              title="Завантажити Excel звіт (.xlsx)"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
              <span className="hidden sm:inline">Excel</span>
            </button>

            <button
              onClick={() => {
                setEmailForm({
                  ...emailForm,
                  month: selectedMonth,
                  from: fromDate,
                  to: toDate,
                  region: selectedRegion
                })
                setIsEmailModalOpen(true)
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition shadow-xs cursor-pointer"
            >
              <Mail className="w-4 h-4" />
              <span>Email-розсилка</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        
        {/* Filters Toolbar */}
        <section className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
            <div className="flex items-center gap-2 text-sm font-semibold text-slate-800">
              <Filter className="w-4 h-4 text-blue-600" />
              <span>Фільтри періоду та даних</span>
              {summary && (
                <span className="text-xs text-blue-700 font-medium ml-2 bg-blue-50 px-2.5 py-0.5 rounded-full border border-blue-200">
                  {summary.periodTitle}
                </span>
              )}
            </div>

            <button
              onClick={() => {
                setSelectedMonth('')
                setSelectedRegion('')
                setFromDate('')
                setToDate('')
                setSearchTerm('')
              }}
              className="text-xs text-slate-500 hover:text-slate-800 flex items-center gap-1 transition cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span>Скинути фільтри</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
            {/* Quick Month Select */}
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1.5">Місяць (РРРР-ММ)</label>
              <input
                type="month"
                value={selectedMonth}
                onChange={(e) => {
                  setSelectedMonth(e.target.value)
                  if (e.target.value) {
                    setFromDate('')
                    setToDate('')
                  }
                }}
                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:outline-none focus:bg-white focus:border-blue-500 transition"
              />
            </div>

            {/* Region Filter */}
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1.5">Регіон</label>
              <select
                value={selectedRegion}
                onChange={(e) => setSelectedRegion(e.target.value)}
                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:outline-none focus:bg-white focus:border-blue-500 transition"
              >
                <option value="">Всі регіони</option>
                {REGIONS.map((r) => (
                  <option key={r} value={r}>{r}</option>
                ))}
              </select>
            </div>

            {/* From Date */}
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1.5">Дата з</label>
              <input
                type="date"
                value={fromDate}
                onChange={(e) => {
                  setFromDate(e.target.value)
                  if (e.target.value) setSelectedMonth('')
                }}
                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:outline-none focus:bg-white focus:border-blue-500 transition"
              />
            </div>

            {/* To Date */}
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1.5">Дата по</label>
              <input
                type="date"
                value={toDate}
                onChange={(e) => {
                  setToDate(e.target.value)
                  if (e.target.value) setSelectedMonth('')
                }}
                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:outline-none focus:bg-white focus:border-blue-500 transition"
              />
            </div>
          </div>
        </section>

        {/* KPI Summary Cards */}
        {summary && (
          <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 relative overflow-hidden shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <p className="text-xs font-bold uppercase tracking-wider text-blue-700">Загальний виторг</p>
                <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
                  <DollarSign className="w-4 h-4" />
                </div>
              </div>
              <h3 className="text-2xl font-extrabold text-slate-900">{formatCurrency(summary.totalAmount)}</h3>
              <p className="text-xs text-slate-500 mt-2 flex items-center gap-1">
                <span className="text-emerald-600 font-semibold">100%</span> за обраний період
              </p>
            </div>

            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 relative overflow-hidden shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <p className="text-xs font-bold uppercase tracking-wider text-emerald-700">Кількість угод</p>
                <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
                  <ShoppingBag className="w-4 h-4" />
                </div>
              </div>
              <h3 className="text-2xl font-extrabold text-slate-900">{summary.totalSalesCount}</h3>
              <p className="text-xs text-slate-500 mt-2">Успішно закритих угод</p>
            </div>

            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 relative overflow-hidden shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <p className="text-xs font-bold uppercase tracking-wider text-purple-700">Середній чек</p>
                <div className="w-8 h-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
                  <TrendingUp className="w-4 h-4" />
                </div>
              </div>
              <h3 className="text-2xl font-extrabold text-slate-900">{formatCurrency(summary.averageCheck)}</h3>
              <p className="text-xs text-slate-500 mt-2">Середня сума 1 угоди</p>
            </div>

            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 relative overflow-hidden shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <p className="text-xs font-bold uppercase tracking-wider text-amber-700">Лідер продажів</p>
                <div className="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
                  <Users className="w-4 h-4" />
                </div>
              </div>
              <h3 className="text-lg font-bold text-slate-900 truncate">
                {summary.managerSummaries && summary.managerSummaries[0]
                  ? summary.managerSummaries[0].manager
                  : '—'}
              </h3>
              <p className="text-xs text-slate-500 mt-2 truncate">
                {summary.managerSummaries && summary.managerSummaries[0]
                  ? `${formatCurrency(summary.managerSummaries[0].totalAmount)} (${summary.managerSummaries[0].percentage}%)`
                  : 'Немає даних'}
              </p>
            </div>
          </section>
        )}

        {/* Analytics Breakdown Grid */}
        {summary && (
          <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Regions Breakdown */}
            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-xs">
              <div className="flex items-center justify-between mb-4 pb-2 border-b border-slate-100">
                <h4 className="font-bold text-sm text-slate-800 flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-blue-600" />
                  <span>Розподіл за регіонами</span>
                </h4>
                <span className="text-xs text-slate-500 font-medium">{summary.regionSummaries.length} регіонів</span>
              </div>

              <div className="space-y-3.5">
                {summary.regionSummaries.length === 0 ? (
                  <p className="text-sm text-slate-400 text-center py-4">Немає даних за обраний період</p>
                ) : (
                  summary.regionSummaries.map((r) => (
                    <div key={r.region} className="space-y-1.5">
                      <div className="flex justify-between text-xs font-medium">
                        <span className="text-slate-700">{r.region}</span>
                        <div className="flex items-center gap-2">
                          <span className="text-slate-400">{r.count} угод</span>
                          <span className="text-slate-900 font-bold">{formatCurrency(r.totalAmount)}</span>
                          <span className="text-blue-600 font-semibold w-12 text-right">({r.percentage}%)</span>
                        </div>
                      </div>
                      <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-blue-500 to-indigo-600 rounded-full transition-all duration-500"
                          style={{ width: `${Math.min(100, Math.max(2, r.percentage))}%` }}
                        />
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Top Managers Breakdown */}
            <div className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-xs">
              <div className="flex items-center justify-between mb-4 pb-2 border-b border-slate-100">
                <h4 className="font-bold text-sm text-slate-800 flex items-center gap-2">
                  <Users className="w-4 h-4 text-indigo-600" />
                  <span>Топ-продавці (по менеджерах)</span>
                </h4>
                <span className="text-xs text-slate-500 font-medium">{summary.managerSummaries.length} менеджерів</span>
              </div>

              <div className="space-y-2.5">
                {summary.managerSummaries.length === 0 ? (
                  <p className="text-sm text-slate-400 text-center py-4">Немає даних за обраний період</p>
                ) : (
                  summary.managerSummaries.map((m, idx) => (
                    <div key={m.manager} className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200/60 hover:bg-slate-100/60 transition">
                      <div className="flex items-center gap-3">
                        <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                          idx === 0 ? 'bg-amber-100 text-amber-800 border border-amber-300' :
                          idx === 1 ? 'bg-slate-200 text-slate-700 border border-slate-300' :
                          idx === 2 ? 'bg-orange-100 text-orange-800 border border-orange-300' :
                          'bg-slate-100 text-slate-600 border border-slate-200'
                        }`}>
                          {idx + 1}
                        </div>
                        <div>
                          <p className="text-xs font-semibold text-slate-800">{m.manager}</p>
                          <p className="text-[11px] text-slate-500">{m.count} закритих угод</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-xs font-bold text-slate-900">{formatCurrency(m.totalAmount)}</p>
                        <p className="text-[11px] text-indigo-600 font-medium">{m.percentage}% частка</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </section>
        )}

        {/* Detailed Sales Registry Table */}
        <section className="bg-white border border-slate-200/90 rounded-2xl overflow-hidden shadow-xs">
          <div className="p-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h4 className="font-bold text-base text-slate-900">Реєстр продажів</h4>
              <p className="text-xs text-slate-500 mt-0.5">Повний список транзакцій з деталями</p>
            </div>

            {/* Search Input */}
            <div className="relative w-full sm:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
              <input
                type="text"
                placeholder="Пошук менеджера, товару..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full bg-slate-50 border border-slate-300 rounded-xl pl-9 pr-3 py-1.5 text-xs text-slate-800 focus:outline-none focus:bg-white focus:border-blue-500 transition"
              />
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50/80 text-slate-600 uppercase tracking-wider font-semibold border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3.5">№</th>
                  <th className="px-5 py-3.5">Дата</th>
                  <th className="px-5 py-3.5">Менеджер</th>
                  <th className="px-5 py-3.5">Товар / Послуга</th>
                  <th className="px-5 py-3.5">Регіон</th>
                  <th className="px-5 py-3.5 text-right">Сума</th>
                  <th className="px-5 py-3.5 text-center">Дія</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {loading ? (
                  <tr>
                    <td colSpan="7" className="text-center py-10 text-slate-400">
                      <RefreshCw className="w-6 h-6 animate-spin mx-auto text-blue-600 mb-2" />
                      Завантаження даних...
                    </td>
                  </tr>
                ) : filteredSales.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center py-12 text-slate-400">
                      Записів не знайдено за поточними фільтрами
                    </td>
                  </tr>
                ) : (
                  filteredSales.map((sale, idx) => (
                    <tr key={sale.id} className="hover:bg-slate-50/70 transition">
                      <td className="px-5 py-3.5 text-slate-400 font-mono">{idx + 1}</td>
                      <td className="px-5 py-3.5 whitespace-nowrap text-slate-600">{sale.date}</td>
                      <td className="px-5 py-3.5 font-medium text-slate-900">{sale.manager}</td>
                      <td className="px-5 py-3.5 text-slate-700">{sale.product}</td>
                      <td className="px-5 py-3.5">
                        <span className="px-2.5 py-1 rounded-full text-[11px] font-medium bg-blue-50 text-blue-700 border border-blue-200">
                          {sale.region}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-right font-bold text-slate-900 whitespace-nowrap">
                        {formatCurrency(sale.amount)}
                      </td>
                      <td className="px-5 py-3.5 text-center">
                        <button
                          onClick={() => handleDeleteSale(sale.id)}
                          className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition cursor-pointer"
                          title="Видалити запис"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      {/* Add Sale Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4 animate-fadeIn">
          <div className="bg-white border border-slate-200 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Plus className="w-5 h-5 text-blue-600" />
                <span>Додати новий продаж</span>
              </h3>
              <button onClick={() => setIsAddModalOpen(false)} className="text-slate-400 hover:text-slate-700 cursor-pointer">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleAddSale} className="space-y-3.5 text-sm">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Ім'я менеджера *</label>
                <input
                  type="text"
                  required
                  placeholder="напр. Олександр Коваленко"
                  value={newSale.manager}
                  onChange={(e) => setNewSale({ ...newSale, manager: e.target.value })}
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2 text-slate-900 focus:outline-none focus:bg-white focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Товар / Послуга *</label>
                <input
                  type="text"
                  required
                  placeholder="напр. Ліцензія CRM Enterprise"
                  value={newSale.product}
                  onChange={(e) => setNewSale({ ...newSale, product: e.target.value })}
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2 text-slate-900 focus:outline-none focus:bg-white focus:border-blue-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-700 mb-1">Сума (₴) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    placeholder="50000.00"
                    value={newSale.amount}
                    onChange={(e) => setNewSale({ ...newSale, amount: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2 text-slate-900 focus:outline-none focus:bg-white focus:border-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-700 mb-1">Дата *</label>
                  <input
                    type="date"
                    required
                    value={newSale.date}
                    onChange={(e) => setNewSale({ ...newSale, date: e.target.value })}
                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2 text-slate-900 focus:outline-none focus:bg-white focus:border-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Регіон *</label>
                <select
                  value={newSale.region}
                  onChange={(e) => setNewSale({ ...newSale, region: e.target.value })}
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2 text-slate-900 focus:outline-none focus:bg-white focus:border-blue-500"
                >
                  {REGIONS.map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-slate-700 text-xs font-medium transition cursor-pointer"
                >
                  Скасувати
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-medium transition shadow-sm cursor-pointer"
                >
                  Зберегти продаж
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Email Sending Modal */}
      {isEmailModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4 animate-fadeIn">
          <div className="bg-white border border-slate-200 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Mail className="w-5 h-5 text-indigo-600" />
                <span>Відправити PDF-звіт на Email</span>
              </h3>
              <button onClick={() => setIsEmailModalOpen(false)} className="text-slate-400 hover:text-slate-700 cursor-pointer">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSendEmail} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">
                  Емейл отримувача / отримувачів (через кому) *
                </label>
                <input
                  type="text"
                  required
                  placeholder="director@company.com, head@sales.ua"
                  value={emailForm.emails}
                  onChange={(e) => setEmailForm({ ...emailForm, emails: e.target.value })}
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3.5 py-2.5 text-slate-900 focus:outline-none focus:bg-white focus:border-indigo-500"
                />
                <p className="text-[11px] text-slate-500 mt-1">
                  Можна вказати кілька адрес, розділяючи їх комами. До листа буде прикріплено згенерований <strong>PDF-файл</strong>.
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3 bg-slate-50 p-3.5 rounded-xl border border-slate-200/80">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Місяць звіту</label>
                  <input
                    type="month"
                    value={emailForm.month}
                    onChange={(e) => setEmailForm({ ...emailForm, month: e.target.value })}
                    className="w-full bg-white border border-slate-300 rounded-lg px-2.5 py-1.5 text-xs text-slate-800"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Фільтр регіону</label>
                  <select
                    value={emailForm.region}
                    onChange={(e) => setEmailForm({ ...emailForm, region: e.target.value })}
                    className="w-full bg-white border border-slate-300 rounded-lg px-2.5 py-1.5 text-xs text-slate-800"
                  >
                    <option value="">Всі регіони</option>
                    {REGIONS.map((r) => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsEmailModalOpen(false)}
                  disabled={emailLoading}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-slate-700 text-xs font-medium transition cursor-pointer"
                >
                  Скасувати
                </button>
                <button
                  type="submit"
                  disabled={emailLoading}
                  className="flex items-center gap-1.5 px-5 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-xs font-medium transition shadow-sm cursor-pointer"
                >
                  {emailLoading ? (
                    <>
                      <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                      <span>Генерація та відправка...</span>
                    </>
                  ) : (
                    <>
                      <Send className="w-3.5 h-3.5" />
                      <span>Надіслати звіт</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500 mt-auto">
        <p>DeeployRKD Enterprise © 2026</p>
      </footer>
    </div>
  )
}
